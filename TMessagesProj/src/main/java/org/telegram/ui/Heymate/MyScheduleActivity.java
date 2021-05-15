package org.telegram.ui.Heymate;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.ApiException;
import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.google.android.exoplayer2.util.Log;
import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.celo.CeloError;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class MyScheduleActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private static final String TAG = "MyScheduleActivity";

    private static final String PLACEHOLDER_SUB_CATEGORY = "{sub_category}";
    private static final String PLACEHOLDER_TIME_DIFF = "{time_diff}";
    private static final String PLACEHOLDER_TIME_DIFF_DAY = "{time_diff_d}";
    private static final String PLACEHOLDER_TIME_DIFF_HOUR = "{time_diff_h}";
    private static final String PLACEHOLDER_TIME_DIFF_MINUTE = "{time_diff_m}";

    private static final long ONE_MINUTE = 60L * 1000L;
    private static final long ONE_HOUR = 60L * ONE_MINUTE;
    private static final long ONE_DAY = 24L * ONE_HOUR;

    private List<ScheduleAdapter> mAdapters = new ArrayList<>(2);

    @Override
    public boolean onFragmentCreate() {
        HeymateEvents.register(HeymateEvents.RESERVATION_STATUS_UPDATED, this);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        HeymateEvents.unregister(HeymateEvents.RESERVATION_STATUS_UPDATED, this);

        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.MY_SCHEDULE));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        ViewPagerFixed viewPager = new ViewPagerFixed(context);
        viewPager.setAdapter(new ViewPagerFixed.Adapter() {

            @Override
            public int getItemCount() {
                return 2;
            }

            @Override
            public View createView(int viewType) {
                RecyclerListView view = new RecyclerListView(context);
                view.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
                return view;
            }

            @Override
            public void bindView(View view, int position, int viewType) {
                bindAdapter((RecyclerListView) view, position);
            }

            @Override
            public String getItemTitle(int position) {
                return Texts.get(position == 0 ? Texts.MY_SCHEDULE_OFFERS : Texts.MY_SCHEDULE_ORDERS).toString();
            }

        });

        ViewPagerFixed.TabsView tabsView = viewPager.createTabsView();

        content.addView(tabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 44));
        content.addView(viewPager, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        fragmentView = content;

        return fragmentView;
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();
        mAdapters.clear();
        super.clearViews();
    }

    private void bindAdapter(RecyclerListView listView, int position) {
        if (listView.getAdapter() == null) {
            ScheduleAdapter adapter = new ScheduleAdapter(listView, position == 0);
            listView.setAdapter(adapter);
            mAdapters.add(adapter);
        }

        ((ScheduleAdapter) listView.getAdapter()).getData();
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);
        return super.onBackPressed();
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        String id = (String) args[0];

        HtAmplify.getInstance(getParentActivity()).getReservation(id, (success, result, exception) -> {
            if (success && result != null) {
                for (ScheduleAdapter adapter: mAdapters) {
                    adapter.updateReservation(result);
                }
            }
        });
    }

    private class ScheduleAdapter extends RecyclerListView.SectionsAdapter {

        private final RecyclerListView mListView;
        private final boolean mIsMyOffers;

        private SparseArray<List<Reservation>> mSections = new SparseArray<>();
        private Map<String, Offer> mOffers = new HashMap<>();

        public ScheduleAdapter(RecyclerListView listView, boolean isMyOffers) {
            mListView = listView;
            mIsMyOffers = isMyOffers;
        }

        public void getData() {
            if (mIsMyOffers) {
                HtAmplify.getInstance(getParentActivity()).getMyAcceptedOffers(this::onReservationsQueryResult);
            }
            else {
                HtAmplify.getInstance(getParentActivity()).getMyOrders(this::onReservationsQueryResult);
            }
        }

        private void onReservationsQueryResult(boolean success, List<Reservation> reservations, ApiException exception) {
            if (!success) {
                return;
            }

            Collections.sort(reservations, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long baseTime = calendar.getTimeInMillis();

            mSections.clear();

            for (Reservation reservation: reservations) {
                long slotTime = reservation.getStartTime() * 1000L;

                int dayDiff;

                if (baseTime > slotTime) {
                    dayDiff = (int) ((baseTime - slotTime) / ONE_DAY + 1);
                }
                else {
                    dayDiff = (int) -((slotTime - baseTime) / ONE_DAY);
                }

                List<Reservation> list = mSections.get(dayDiff);

                if (list == null) {
                    list = new ArrayList<>();
                    mSections.put(dayDiff, list);
                }

                list.add(reservation);

                if (!mOffers.containsKey(reservation.getId())) {
                    HtAmplify.getInstance(getParentActivity()).getOffer(reservation.getOfferId(), (success1, data, exception1) -> {
                        if (success1) {
                            mOffers.put(reservation.getId(), data);
                            updateItem(reservation);
                        }
                    });
                }
            }

            notifyDataSetChanged();
        }

        private void updateItem(Reservation reservation) {
            for (int i = 0; i < mListView.getChildCount(); i++) {
                if (mListView.getChildAt(i) instanceof ScheduleItem) {
                    ScheduleItem item = (ScheduleItem) mListView.getChildAt(i);

                    if (reservation.getId().equals(item.mReservation.getId())) {
                        item.setReservation(reservation);
                        item.setOffer(mOffers.get(reservation.getId()));
                    }
                }
            }
        }

        private void updateReservation(Reservation reservation) {
            boolean found = false;

            for (int i = 0; i < mSections.size(); i++) {
                List<Reservation> reservations = mSections.get(mSections.keyAt(i));

                if (reservations != null) {
                    for (int index = 0; index < reservations.size(); index++) {
                        if (reservation.getId().equals(reservations.get(index).getId())) {
                            found = true;

                            reservations.set(index, reservation);
                            break;
                        }
                    }
                }

                if (found) {
                    break;
                }
            }

            if (found) {
                updateItem(reservation);
            }
        }

        @Override
        public int getSectionCount() {
            return mSections.size();
        }

        @Override
        public int getCountForSection(int section) {
            return mSections.get(mSections.keyAt(section)).size();
        }

        @Override
        public View getSectionHeaderView(int section, View view) {
            TextView textView;

            if (view == null) {
                textView = new TextView(getParentActivity());
                textView.setTextColor(ContextCompat.getColor(getParentActivity(), works.heymate.beta.R.color.ht_theme));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
                textView.setGravity(Gravity.CENTER_VERTICAL);
                textView.setPadding(
                        AndroidUtilities.dp(20),
                        AndroidUtilities.dp(16),
                        AndroidUtilities.dp(20),
                        AndroidUtilities.dp(12)
                );
            }
            else {
                textView = (TextView) view;
            }

            textView.setText(getTitleForDayDifference(mSections.keyAt(section)));

            return textView;
        }

        @Override
        public boolean isEnabled(int section, int row) {
            return row != 0;
        }

        @Override
        public int getItemViewType(int section, int position) {
            return 0;
        }

        @Override
        public Object getItem(int section, int position) {
            return mSections.get(mSections.keyAt(section)).get(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ScheduleItem view = new ScheduleItem(parent.getContext());
            view.setIsMyOffer(mIsMyOffers);

            return new RecyclerView.ViewHolder(view) { };
        }

        @Override
        public void onBindViewHolder(int section, int position, RecyclerView.ViewHolder holder) {
            ScheduleItem item = (ScheduleItem) holder.itemView;

            Reservation reservation = (Reservation) getItem(section, position);

            item.setReservation(reservation);
            item.setOffer(mOffers.get(reservation.getId()));
        }

        @Override
        public String getLetter(int position) {
            return null;
        }

        @Override
        public int getPositionForScrollProgress(float progress) {
            return 0;
        }

        private CharSequence getTitleForDayDifference(int dayDiff) {
            switch (dayDiff) {
                case 0:
                    return Texts.get(Texts.TODAY);
                case 1:
                    return Texts.get(Texts.YESTERDAY);
                default:
                    List<Reservation> reservations = mSections.get(dayDiff);

                    if (reservations == null || reservations.isEmpty()) {
                        return "";
                    }

                    return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(reservations.get(0).getStartTime() * 1000L);
            }
        }

    }

    private class ScheduleItem extends SequenceLayout implements View.OnClickListener {

        private final ImageView mImageUser;
        private final TextView mTextName;
        private final TextView mTextInfo;
        private final TextView mButtonLeft;
        private final TextView mButtonRight;

        private boolean mIsMyOffer;

        private Reservation mReservation = null;
        private Offer mOffer = null;

        private int mUserId = 0;

        private ImageReceiver avatarImage = new ImageReceiver(this);
        private AvatarDrawable avatarDrawable = new AvatarDrawable();

        public ScheduleItem(Context context) {
            super(context);
            setWillNotDraw(false);

            setPageWidth(140);
            setPageHeight(239.7f);
            LayoutInflater.from(context).inflate(works.heymate.beta.R.layout.item_schedule, this, true);
            addSequences(works.heymate.beta.R.xml.sequences_item_schedule);

            mImageUser = findViewById(works.heymate.beta.R.id.image_user);
            mTextName = findViewById(works.heymate.beta.R.id.text_name);
            mTextInfo = findViewById(works.heymate.beta.R.id.text_info);
            mButtonLeft = findViewById(works.heymate.beta.R.id.button_left);
            mButtonRight = findViewById(works.heymate.beta.R.id.button_right);

            mTextName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
            mTextInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

            mButtonLeft.setText(Texts.get(Texts.CANCEL));
            mButtonLeft.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            mButtonLeft.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_windowBackgroundGray)));

            mImageUser.setOnClickListener(this);
            mTextName.setOnClickListener(this);
            mButtonLeft.setOnClickListener(this);

            avatarImage.setRoundRadius(AndroidUtilities.dp(28));
        }

        public void setIsMyOffer(boolean isMyOffer) {
            mIsMyOffer = isMyOffer;
        }

        public void setReservation(Reservation reservation) {
            mReservation = reservation;

            if (mIsMyOffer) {
                try {
                    mUserId = Integer.parseInt(reservation.getConsumerId());
                } catch (Throwable t) {
                    mUserId = 0;
                }
            }

            updateLayout();
        }

        public void setOffer(Offer offer) {
            mOffer = offer;

            if (!mIsMyOffer) {
                try {
                    mUserId = Integer.parseInt(offer.getUserId());
                } catch (Throwable t) {
                    mUserId = 0;
                }
            }

            updateLayout();
        }

        private void updateLayout() {
            if (mUserId != 0) {
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(mUserId);
                onUserLoaded(user);
            }
            else {
                onUserLoaded(null);
            }

            if (mOffer != null && mReservation != null) {
                String text;

                HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(mReservation.getStatus());

                switch (status) {
                    case BOOKED:
                        text = Texts.get(Texts.MY_SCHEDULE_ACCEPTED).toString();
                        break;
                    case CANCELLED:
                        text = Texts.get(Texts.MY_SCHEDULE_CANCELLED).toString();
                        break;
                    case MARKED_AS_STARTED:
                        text = Texts.get(Texts.MY_SCHEDULE_MARKED_STARTED).toString();
                        break;
                    case STARTED:
                        text = Texts.get(Texts.MY_SCHEDULE_STARTED).toString();
                        break;
                    case MARKED_AS_FINISHED:
                        text = Texts.get(Texts.MY_SCHEDULE_MARKED_FINISHED).toString();
                        break;
                    case FINISHED:
                        text = Texts.get(Texts.MY_SCHEDULE_FINISHED).toString();
                        break;
                    default:
                        text = "";
                        break;
                }

                text = text
                        .replace(PLACEHOLDER_SUB_CATEGORY, mOffer.getSubCategory())
                        .replace(PLACEHOLDER_TIME_DIFF, getTimeDiff(mReservation.getStartTime() * 1000L));

                mTextInfo.setText(text);
            }
            else {
                mTextInfo.setText("");
            }

            if (mReservation != null) {
                HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(mReservation.getStatus());

                switch (status) {
                    case BOOKED:
                        mButtonLeft.setVisibility(VISIBLE);
                        enableLeft();
                        if (mIsMyOffer) {
                            setRightPositive();
                            mButtonRight.setText(Texts.get(Texts.START));
                            mButtonRight.setOnClickListener(v -> markAsStarted());
                        }
                        else {
                            setRightAsDetails();
                        }
                        break;
                    case FINISHED:
                    case CANCELLED:
                        mButtonLeft.setVisibility(GONE);
                        setRightAsDetails();
                        break;
                    case MARKED_AS_STARTED:
                        if (mIsMyOffer) {
                            mButtonLeft.setVisibility(GONE);
                            setRightAsDetails();
                        }
                        else {
                            mButtonLeft.setVisibility(VISIBLE);
                            enableLeft();
                            setRightPositive();
                            mButtonRight.setText(Texts.get(Texts.CONFIRM));
                            mButtonRight.setOnClickListener(v -> confirmStarted());
                        }
                        break;
                    case STARTED:
                        mButtonLeft.setVisibility(GONE);
                        if (mIsMyOffer) {
                            setRightPositive();
                            mButtonRight.setText(Texts.get(Texts.FINISH));
                            mButtonRight.setOnClickListener(v -> markAsFinished());
                        }
                        else {
                            setRightAsDetails();
                        }
                        break;
                    case MARKED_AS_FINISHED:
                        mButtonLeft.setVisibility(GONE);
                        if (mIsMyOffer) {
                            setRightAsDetails();
                        }
                        else {
                            setRightPositive();
                            mButtonRight.setText(Texts.get(Texts.CONFIRM));
                            mButtonRight.setOnClickListener(v -> confirmFinished());
                        }
                        break;
                }
            }
            else {
                mButtonLeft.setVisibility(GONE);
                mButtonRight.setVisibility(GONE);
            }
        }

        private void onUserLoaded(TLRPC.User user) {
            mTextName.setText(user == null ? "" : UserObject.getUserName(user));

            avatarDrawable.setInfo(user);
            avatarImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, null, user, 0);
        }

        private void setRightAsDetails() {
            setRightNeutral();
            mButtonRight.setText(Texts.get(Texts.DETAILS));
            mButtonRight.setOnClickListener(v -> showDetails());
        }

        private void setRightPositive() {
            mButtonRight.setVisibility(VISIBLE);
            mButtonRight.setEnabled(true);
            mButtonRight.setAlpha(1);
            mButtonRight.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
            mButtonRight.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), ContextCompat.getColor(getContext(), works.heymate.beta.R.color.ht_theme)));
        }

        private void setRightNeutral() {
            mButtonRight.setVisibility(VISIBLE);
            mButtonRight.setEnabled(true);
            mButtonRight.setAlpha(1);
            mButtonRight.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
            mButtonRight.setBackground(Theme.createBorderRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_divider)));
        }

        private void disableRight() {
            mButtonRight.setEnabled(false);
            mButtonRight.setAlpha(0.5f);
        }

        private void disableLeft() {
            mButtonLeft.setEnabled(false);
            mButtonLeft.setAlpha(0.5f);
        }

        private void enableLeft() {
            mButtonLeft.setEnabled(true);
            mButtonLeft.setAlpha(1);
        }

        @Override
        public void onClick(View v) {
            if (v == mImageUser || v == mTextName) {
                if (mUserId != 0) {
                    Bundle args = new Bundle();
                    args.putInt("user_id", mUserId);
                    presentFragment(new ProfileActivity(args));
                }
                return;
            }

            if (v == mButtonLeft) {
                cancel();
                return;
            }
        }

        private void cancel() {
            if (mOffer == null || mReservation == null) {
                return;
            }

            AlertDialog loading = new AlertDialog(getContext(), 3);
            loading.setCanCacnel(false);
            loading.show();

            Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

            boolean cancelledByConsumer = mReservation.getConsumerId().equals(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId));

            wallet.cancelOffer(mOffer, mReservation, cancelledByConsumer, (success, errorCause) -> {
                loading.dismiss();

                if (success) {
                    HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.CANCELLED);
                }
                else {
                    Log.e(TAG, "Failed to cancel offer", errorCause);
                    LogToGroup.log("Failed to cancel offer", errorCause, MyScheduleActivity.this);

                    if (errorCause != null) {
                        CeloError coreError = errorCause.getMainCause().getError();

                        if (coreError == CeloError.NETWORK_ERROR) {
                            Toast.makeText(getContext(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                    }
                }
            });

//            HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.CANCELLED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void markAsStarted() {
            HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.MARKED_AS_STARTED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void confirmStarted() {
            if (mOffer == null || mReservation == null) {
                return;
            }

            AlertDialog loading = new AlertDialog(getContext(), 3);
            loading.setCanCacnel(false);
            loading.show();

            Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

            wallet.startOffer(mOffer, mReservation, (success, errorCause) -> {
                loading.dismiss();

                if (success) {
                    HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.STARTED);
                }
                else {
                    Log.e(TAG, "Failed to confirm started offer", errorCause);
                    LogToGroup.log("Failed to confirm started offer", errorCause, MyScheduleActivity.this);

                    if (errorCause != null) {
                        CeloError coreError = errorCause.getMainCause().getError();

                        if (coreError == CeloError.NETWORK_ERROR) {
                            Toast.makeText(getContext(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                    }
                }
            });

//            HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.STARTED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void markAsFinished() {
            HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.MARKED_AS_FINISHED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void confirmFinished() {
            if (mOffer == null || mReservation == null) {
                return;
            }

            AlertDialog loading = new AlertDialog(getContext(), 3);
            loading.setCanCacnel(false);
            loading.show();

            Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

            wallet.finishOffer(mOffer, mReservation, (success, errorCause) -> {
                loading.dismiss();

                if (success) {
                    HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.FINISHED);
                }
                else {
                    Log.e(TAG, "Failed to confirm finished offer", errorCause);
                    LogToGroup.log("Failed to confirm finished offer", errorCause, MyScheduleActivity.this);

                    if (errorCause != null) {
                        CeloError coreError = errorCause.getMainCause().getError();

                        if (coreError == CeloError.NETWORK_ERROR) {
                            Toast.makeText(getContext(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                        }
                    }
                    else {
                        Toast.makeText(getContext(), Texts.get(Texts.UNKNOWN_ERROR), Toast.LENGTH_LONG).show();
                    }
                }
            });

//            HtAmplify.getInstance(getParentActivity()).updateReservation(mReservation, HtTimeSlotStatus.FINISHED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void showDetails() {
            HtOfferDetailsPopUp detailsPopUp = new HtOfferDetailsPopUp(getContext(), MyScheduleActivity.this,  0, mOffer, null);
            AlertDialog dialog = detailsPopUp.create();
            detailsPopUp.closeImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            showDialog(dialog);
            // TODO
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);

            avatarImage.setImageCoords(mImageUser.getLeft(), mImageUser.getTop(), mImageUser.getWidth(), mImageUser.getHeight());
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            avatarImage.draw(canvas);
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();

            avatarImage.onAttachedToWindow();
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();

            avatarImage.onDetachedFromWindow();
        }

    }

    private String getTimeDiff(long time) {
        long now = System.currentTimeMillis();

        long diff = now - time;

        if (diff < 0) {
            diff = 0;
        }

        int days = (int) (diff / ONE_DAY);
        int hours = (int) ((diff % ONE_DAY) / ONE_HOUR);
        int minutes = (int) ((diff % ONE_HOUR) / ONE_MINUTE);

        if (days > 0) {
            return Texts.get(Texts.TIME_DIFF_DAY).toString().replace(PLACEHOLDER_TIME_DIFF_DAY, String.valueOf(hours));
        }

        return hours > 0 ?
                Texts.get(Texts.TIME_DIFF_HOUR).toString().replace(PLACEHOLDER_TIME_DIFF_HOUR, String.valueOf(hours)) :
                Texts.get(Texts.TIME_DIFF_MINUTE).toString().replace(PLACEHOLDER_TIME_DIFF_MINUTE, String.valueOf(minutes));
    }

}
