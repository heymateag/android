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

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.api.ApiException;
import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.messenger.time.FastDateFormat;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.ViewPagerFixed;
import org.telegram.ui.Heymate.AmplifyModels.Offer;
import org.telegram.ui.Heymate.AmplifyModels.TimeSlot;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;

public class MyScheduleActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

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
        HeymateEvents.register(HeymateEvents.ACCEPTED_OFFER_STATUS_UPDATED, this);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        HeymateEvents.unregister(HeymateEvents.ACCEPTED_OFFER_STATUS_UPDATED, this);

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

        HtAmplify.getInstance(getParentActivity()).getTimeSlot(id, (success, result, exception) -> {
            if (success) {
                for (ScheduleAdapter adapter: mAdapters) {
                    adapter.updateTimeSlot(result);
                }
            }
        });
    }

    private class ScheduleAdapter extends RecyclerListView.SectionsAdapter implements
            HtAmplify.TimeSlotsCallback, HtAmplify.OffersCallback {

        private final RecyclerListView mListView;
        private final boolean mIsMyOffers;

        private SparseArray<List<TimeSlot>> mSections = new SparseArray<>();
        private Map<String, Offer> mOffers = new HashMap<>();

        public ScheduleAdapter(RecyclerListView listView, boolean isMyOffers) {
            mListView = listView;
            mIsMyOffers = isMyOffers;
        }

        public void getData() {
            if (mIsMyOffers) {
                HtAmplify.getInstance(getParentActivity()).getOffersWithoutImages(this);
            }
            else {
                int userId = UserConfig.getInstance(currentAccount).clientUserId;
                HtAmplify.getInstance(getParentActivity()).getMyOrders("" + userId, this);
            }
        }

        @Override
        public void onOffersQueryResult(boolean success, List<Offer> offers, ApiException exception) {
            if (!success) {
                return;
            }

            List<TimeSlot> timeSlots = new ArrayList<>();

            getTimeSlotsForOffer(offers, timeSlots);
        }

        private void getTimeSlotsForOffer(List<Offer> offers, List<TimeSlot> timeSlots) {
            if (offers.isEmpty()) {
                onTimeSlotsQueryResult(true, timeSlots, null);
                return;
            }

            Offer offer = offers.remove(0);

            HtAmplify.getInstance(getParentActivity()).getNonAvailableTimeSlots(offer.getId(), (success, offerTimeSlots, exception) -> {
                if (success) {
                    timeSlots.addAll(offerTimeSlots);

                    for (TimeSlot timeSlot: offerTimeSlots) {
                        mOffers.put(timeSlot.getId(), offer);
                    }
                }

                getTimeSlotsForOffer(offers, timeSlots);
            });
        }

        @Override
        public void onTimeSlotsQueryResult(boolean success, List<TimeSlot> timeSlots, ApiException exception) {
            if (!success) {
                return;
            }

            Collections.sort(timeSlots, (o1, o2) -> o2.getStartTime() - o1.getStartTime());

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long baseTime = calendar.getTimeInMillis();

            mSections.clear();

            for (TimeSlot timeSlot: timeSlots) {
                long slotTime = timeSlot.getStartTime() * 1000L;

                int dayDiff;

                if (baseTime > slotTime) {
                    dayDiff = (int) ((baseTime - slotTime) / ONE_DAY + 1);
                }
                else {
                    dayDiff = (int) -((slotTime - baseTime) / ONE_DAY);
                }

                List<TimeSlot> list = mSections.get(dayDiff);

                if (list == null) {
                    list = new ArrayList<>();
                    mSections.put(dayDiff, list);
                }

                list.add(timeSlot);

                if (!mOffers.containsKey(timeSlot.getId())) {
                    HtAmplify.getInstance(getParentActivity()).getOffer(timeSlot.getOfferId(), (success1, data, exception1) -> {
                        if (success1) {
                            mOffers.put(timeSlot.getId(), data);
                            updateItem(timeSlot);
                        }
                    });
                }
            }

            notifyDataSetChanged();
        }

        private void updateItem(TimeSlot timeSlot) {
            for (int i = 0; i < mListView.getChildCount(); i++) {
                if (mListView.getChildAt(i) instanceof ScheduleItem) {
                    ScheduleItem item = (ScheduleItem) mListView.getChildAt(i);

                    if (timeSlot.getId().equals(item.mTimeSlot.getId())) {
                        item.setTimeSlot(timeSlot);
                        item.setOffer(mOffers.get(timeSlot.getId()));
                    }
                }
            }
        }

        private void updateTimeSlot(TimeSlot timeSlot) {
            boolean found = false;

            for (int i = 0; i < mSections.size(); i++) {
                List<TimeSlot> timeSlots = mSections.get(mSections.keyAt(i));

                if (timeSlots != null) {
                    for (int index = 0; index < timeSlots.size(); index++) {
                        if (timeSlot.getId().equals(timeSlots.get(index).getId())) {
                            found = true;

                            timeSlots.set(index, timeSlot);
                            break;
                        }
                    }
                }

                if (found) {
                    break;
                }
            }

            if (found) {
                updateItem(timeSlot);
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
                textView.setTextColor(ContextCompat.getColor(getParentActivity(), R.color.ht_theme));
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

            TimeSlot timeSlot = (TimeSlot) getItem(section, position);

            item.setTimeSlot(timeSlot);
            item.setOffer(mOffers.get(timeSlot.getId()));
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
                    List<TimeSlot> timeSlots = mSections.get(dayDiff);

                    if (timeSlots == null || timeSlots.isEmpty()) {
                        return "";
                    }

                    return FastDateFormat.getDateInstance(FastDateFormat.MEDIUM).format(timeSlots.get(0).getStartTime() * 1000L);
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

        private TimeSlot mTimeSlot = null;
        private Offer mOffer = null;

        private int mUserId = 0;

        private ImageReceiver avatarImage = new ImageReceiver(this);
        private AvatarDrawable avatarDrawable = new AvatarDrawable();

        public ScheduleItem(Context context) {
            super(context);
            setWillNotDraw(false);

            setPageWidth(140);
            setPageHeight(239.7f);
            LayoutInflater.from(context).inflate(R.layout.item_schedule, this, true);
            addSequences(R.xml.sequences_item_schedule);

            mImageUser = findViewById(R.id.image_user);
            mTextName = findViewById(R.id.text_name);
            mTextInfo = findViewById(R.id.text_info);
            mButtonLeft = findViewById(R.id.button_left);
            mButtonRight = findViewById(R.id.button_right);

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

        public void setTimeSlot(TimeSlot timeSlot) {
            mTimeSlot = timeSlot;

            if (mIsMyOffer) {
                try {
                    mUserId = Integer.parseInt(timeSlot.getClientUserId());
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

            if (mOffer != null && mTimeSlot != null) {
                String text;

                HtTimeSlotStatus status = HtTimeSlotStatus.values()[mTimeSlot.getStatus()];

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
                        .replace(PLACEHOLDER_TIME_DIFF, getTimeDiff(mTimeSlot.getStartTime() * 1000L));

                mTextInfo.setText(text);
            }
            else {
                mTextInfo.setText("");
            }

            if (mTimeSlot != null) {
                HtTimeSlotStatus status = HtTimeSlotStatus.values()[mTimeSlot.getStatus()];

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
            avatarImage.setImage(ImageLocation.getForUser(user, false), "50_50", avatarDrawable, null, user, 0);
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
            mButtonRight.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), ContextCompat.getColor(getContext(), R.color.ht_theme)));
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
                // TODO Cancel
                return;
            }
        }

        private void markAsStarted() {
            HtAmplify.getInstance(getParentActivity()).updateTimeSlot(mTimeSlot.getId(), HtTimeSlotStatus.MARKED_AS_STARTED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void confirmStarted() {
            HtAmplify.getInstance(getParentActivity()).updateTimeSlot(mTimeSlot.getId(), HtTimeSlotStatus.STARTED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void markAsFinished() {
            HtAmplify.getInstance(getParentActivity()).updateTimeSlot(mTimeSlot.getId(), HtTimeSlotStatus.MARKED_AS_FINISHED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void confirmFinished() {
            HtAmplify.getInstance(getParentActivity()).updateTimeSlot(mTimeSlot.getId(), HtTimeSlotStatus.FINISHED);
            disableLeft();
            disableRight();
            // TODO
        }

        private void showDetails() {
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
