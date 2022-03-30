package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.exoplayer2.util.Log;
import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.log.LogToGroup;
import org.telegram.ui.Heymate.MeetingType;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.offer.OfferDetailsActivity;
import org.telegram.ui.Heymate.onlinemeeting.OnlineMeetingActivity;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.core.Texts;
import works.heymate.core.reservation.ReservationUtils;
import works.heymate.core.wallet.Wallet;
import works.heymate.model.Offer;
import works.heymate.model.TSReservation;
import works.heymate.model.TimeSlot;
import works.heymate.model.User;
import works.heymate.model.Users;

public class MyOfferItem extends SequenceLayout implements View.OnClickListener {

    private static final String TAG = "MyOfferItem";

    private final BaseFragment mParent;

    private final Handler mHandler;

    private final ImageView mImageUser;
    private final TextView mTextName;
    private final TextView mTextInfo;
    private final TextView mButtonLeft;
    private final TextView mButtonRight;

    private boolean mIsOnlineMeeting;

    private APIObject mTimeSlot = null;
    private List<APIObject> mReservations = null;
    private APIObject mOffer = null;

    private String mUserId = null;
    private long mTelegramId = 0;

    private final ImageReceiver avatarImage = new ImageReceiver(this);
    private final AvatarDrawable avatarDrawable = new AvatarDrawable();

    private int mCountDownSeconds = -1;

    public MyOfferItem(Context context, BaseFragment parent) {
        super(context);
        setWillNotDraw(false);

        mParent = parent;

        mHandler = new Handler();

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

    public void setTimeSlot(APIObject timeSlot) {
        boolean timeSlotChanged = mTimeSlot == null || timeSlot == null || !mTimeSlot.getString(TimeSlot.ID).equals(timeSlot.getString(TimeSlot.ID));

        mTimeSlot = timeSlot;

        if (timeSlotChanged) {
            mUserId = null;
        }

        updateIsOnlineMeeting();
        updateLayout();
    }

    public void setReservations(List<APIObject> reservations) {
        mReservations = reservations;

        if (reservations != null && reservations.size() == 1) {
            // TODO fix this! User object is returned instead of reservation.
            mUserId = reservations.get(0).getString(TSReservation.USER_ID);
            mTelegramId = reservations.get(0).getLong(TSReservation.TELEGRAM_ID);
        }
        else {
            mUserId = null;
            mTelegramId = 0;
        }

        if (mTelegramId != 0) {
            TLRPC.User user = MessagesController.getInstance(mParent.getCurrentAccount()).getUser(mTelegramId);
            onUserLoaded(user);
        }
        else if (mUserId != null) {
            Users.getUser(mUserId, result -> {
                if (result.response != null && result.response.getString(User.ID).equals(mUserId)) {
                    mTelegramId = result.response.getLong(User.TELEGRAM_ID);

                    if (mTelegramId != 0) {
                        TLRPC.User user = MessagesController.getInstance(mParent.getCurrentAccount()).getUser(mTelegramId);
                        onUserLoaded(user);
                    }
                    else {
                        onUserLoaded(null);
                    }
                }
                else {
                    onUserLoaded(null);
                }
            });
        }
        else {
            onUserLoaded(null);
        }

        updateLayout();
    }

    public String getTimeSlotId() {
        return mTimeSlot == null ? null : mTimeSlot.getString(TimeSlot.ID);
    }

    public void setOffer(APIObject offer) {
        mOffer = offer;

        updateIsOnlineMeeting();
        updateLayout();
    }

    private void updateIsOnlineMeeting() {
        if (mOffer != null) {
            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mOffer.getString(Offer.MEETING_TYPE));
        }
//        else if (mTimeSlot != null) { // TODO
//            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mTimeSlot.getMeetingType());
//        }
        else {
            mIsOnlineMeeting = false;
        }
    }

    private void updateLayout() {
        stopCountDown();

        if (mReservations != null && mReservations.size() > 1) {
            mTextName.setText(mReservations.size() + " customers."); // TODO Texts
        }

        HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(mTimeSlot.getString(TimeSlot.STATUS));

        if (mOffer != null && mTimeSlot != null) {
            String text;

            switch (status) {
                case BOOKED:
                    text = Texts.get(Texts.MY_SCHEDULE_ACCEPTED).toString();
                    break;
                case CANCELLED_BY_CONSUMER:
                case CANCELLED_BY_SERVICE_PROVIDER:
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
                    .replace(MyScheduleUtils.PLACEHOLDER_SUB_CATEGORY, mOffer.getString(Offer.CATEGORY + "." + Offer.Category.SUB_CATEGORY))
                    .replace(MyScheduleUtils.PLACEHOLDER_TIME_DIFF, MyScheduleUtils.getTimeDiff(mTimeSlot.getLong(TimeSlot.FROM_TIME) * 1000L));

            mTextInfo.setText(text);
        }
        else {
            mTextInfo.setText("");
        }

        enableRight();

        if (status != null) {
            switch (status) {
                case BOOKED:
                    mButtonLeft.setVisibility(VISIBLE);
                    enableLeft();
                    setRightPositive();
                    mButtonRight.setText(Texts.get(mIsOnlineMeeting ? Texts.START_SESSION : Texts.START));
                    mButtonRight.setOnClickListener(v -> markAsStarted());

                    if (mIsOnlineMeeting) {
                        if (mTimeSlot == null || mTimeSlot.getString(TimeSlot.FROM_TIME) == null) {
                            disableRight();
                        }
                        else {
                            long startTime = mTimeSlot.getLong(TimeSlot.FROM_TIME);

                            if (startTime > System.currentTimeMillis() / 1000) {
                                disableRight();
                                startCountDown((int) (startTime - System.currentTimeMillis() / 1000));
                            }
                        }
                    }
                    break;
                case FINISHED:
                case CANCELLED_BY_CONSUMER:
                case CANCELLED_BY_SERVICE_PROVIDER:
                    mButtonLeft.setVisibility(GONE);
                    setRightAsDetails();
                    break;
                case MARKED_AS_STARTED:
                    mButtonLeft.setVisibility(GONE);
                    if (mIsOnlineMeeting) {
                        setRightPositive();
                        mButtonRight.setText(Texts.get(Texts.JOIN_SESSION));
                        mButtonRight.setOnClickListener(v -> joinSession());
                    }
                    else {
                        setRightAsDetails();
                        mButtonRight.setOnClickListener(v -> showDetails());
                    }
                    break;
                case STARTED:
                    mButtonLeft.setVisibility(GONE);
                    if (mIsOnlineMeeting) {
                        setRightPositive();
                        mButtonRight.setText(Texts.get(Texts.JOIN_SESSION));
                        mButtonRight.setOnClickListener(v -> joinSession());
                    }
                    else {
                        setRightPositive();
                        mButtonRight.setText(Texts.get(Texts.FINISH));
                        mButtonRight.setOnClickListener(v -> markAsFinished());
                    }
                    break;
                case MARKED_AS_FINISHED:
                    mButtonLeft.setVisibility(GONE);
                    setRightAsDetails();
                    break;
            }
        }
        else {
            mButtonLeft.setVisibility(GONE);
            mButtonRight.setVisibility(GONE);
        }
    }

    private void startCountDown(int seconds) {
        mHandler.removeCallbacks(mCountDown);

        mCountDownSeconds = seconds;

        mHandler.post(mCountDown);
    }

    private void stopCountDown() {
        mHandler.removeCallbacks(mCountDown);

        mCountDownSeconds = -1;
    }

    private final Runnable mCountDown = new Runnable() {

        @Override
        public void run() {
            setRemainingSeconds(mCountDownSeconds);

            mCountDownSeconds--;

            if (mCountDownSeconds == -1) {
                updateLayout();
            }
            else {
                mHandler.postDelayed(mCountDown, 1000);
            }
        }

    };

    private void setRemainingSeconds(int seconds) {
        int minutes = seconds / 60;
        seconds %= 60;

        int hours = minutes / 60;
        minutes %= 60;

        int days = hours / 24;
        hours %= 24;

        if (days > 0) {
            mButtonRight.setText(days + "d " + hours + "h"); // TODO Centralize relative time
        }
        else {
            mButtonRight.setText(fixDigits(hours) + ":" + fixDigits(minutes) + ":" + fixDigits(seconds));
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

    private void enableRight() {
        mButtonRight.setEnabled(true);
        mButtonRight.setAlpha(1);
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
            if (mTelegramId != 0) {
                Bundle args = new Bundle();
                args.putLong("user_id", mTelegramId);
                mParent.presentFragment(new ProfileActivity(args));
            }
            return;
        }

        if (v == mButtonLeft) {
            cancel();
            return;
        }
    }

    private void cancel() {
        if (mOffer == null || mTimeSlot == null || mReservations == null) {
            return;
        }

        LoadingUtil.onLoadingStarted();

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        ArrayList<APIObject> remainingReservations = new ArrayList<>(mReservations);

        cancelReservations(remainingReservations, wallet, true);

        disableLeft();
        disableRight();
    }

    private void cancelReservations(List<APIObject> remainingReservations, Wallet wallet, boolean noErrors) {
        if (remainingReservations.isEmpty()) {
            LoadingUtil.onLoadingFinished();

            if (noErrors) {
                APIs.get().updateTimeSlot(mTimeSlot.getString(TimeSlot.ID), HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER.name(), mTimeSlot.getString(TimeSlot.MEETING_ID), mTimeSlot.getString(TimeSlot.MEETING_PASSWORD), result -> {
                    // Ignoring this.
                });
            }
            else {
                Toast.makeText(getContext(), Texts.get(Texts.NETWORK_BLOCKCHAIN_ERROR), Toast.LENGTH_LONG).show(); // TODO Not very accurate.
                updateLayout();
            }
            return;
        }

        APIObject reservation = remainingReservations.remove(0);

        wallet.cancelOffer(mOffer, null, reservation, false, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            if (success) {
                cancelReservations(remainingReservations, wallet, noErrors);
            }
            else {
                Log.e(TAG, "Failed to cancel offer", errorCause);
                LogToGroup.log("Failed to cancel offer", errorCause);

                cancelReservations(remainingReservations, wallet, false);
            }
        });
    }

    private void markAsStarted() {
        String meetingId = UUID.randomUUID().toString();
        String meetingPassword = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 10);

        if (mIsOnlineMeeting) {
            LoadingUtil.onLoadingStarted();

            APIs.get().updateTimeSlot(mTimeSlot.getString(TimeSlot.ID), HtTimeSlotStatus.MARKED_AS_STARTED.name(), meetingId, meetingPassword, result -> {
                LoadingUtil.onLoadingFinished();

                if (result.success) {
                    APIs.get().getTimeSlotReservations(mTimeSlot.getString(TimeSlot.ID), reservationsResult -> {
                        if (reservationsResult.success) {
                            APIArray reservations = reservationsResult.response.getArray("data");

                            for (int i = 0; i < reservations.size(); i++) {
                                APIObject tsReservation = reservations.getObject(i);

                                String reservationId = tsReservation.getString(TSReservation.RESERVATION_ID);
                                long telegramId = tsReservation.getLong(TSReservation.TELEGRAM_ID);

                                APIs.get().getReservation(reservationId, reservationResult -> {
                                    if (reservationResult.success) {
                                        String message = ReservationUtils.serializeBeautiful(reservationResult.response, mTimeSlot, mOffer, ReservationUtils.OFFER_ID, ReservationUtils.MEETING_ID, ReservationUtils.MEETING_TYPE, ReservationUtils.START_TIME, ReservationUtils.SERVICE_PROVIDER_ID);
                                        SendMessagesHelper.getInstance(UserConfig.selectedAccount).sendMessage(message, telegramId, null, null, null, false, null, null, null, true, 0, null);
                                    }
                                });
                            }
                        }
                    });

                    joinSession(meetingId, meetingPassword);
                }
                else {
                    Toast.makeText(getContext(), "Network error: " + (result.error == null ? "" : result.error.getMessage()), Toast.LENGTH_LONG).show();
                }
            });
        }
        else {
            APIs.get().updateTimeSlot(mTimeSlot.getString(TimeSlot.ID), HtTimeSlotStatus.MARKED_AS_STARTED.name(), meetingId, meetingPassword, result -> {
                // Ignoring the result
            });

            disableLeft();
            disableRight();
        }
    }

    private void markAsFinished() {
        String meetingId = mTimeSlot.getString(TimeSlot.MEETING_ID);
        String meetingPassword = mTimeSlot.getString(TimeSlot.MEETING_PASSWORD);
        APIs.get().updateTimeSlot(mTimeSlot.getString(TimeSlot.ID), HtTimeSlotStatus.MARKED_AS_FINISHED.name(), meetingId, meetingPassword, result -> {
            // Ignoring
        });

        disableLeft();
        disableRight();
    }

    private void joinSession() {
        joinSession(mTimeSlot.getString(TimeSlot.MEETING_ID), mTimeSlot.getString(TimeSlot.MEETING_PASSWORD));
    }

    private void joinSession(String meetingId, String meetingPassword) {
        LogToGroup.logIfCrashed(() ->
            mParent.presentFragment(new OnlineMeetingActivity(meetingId, meetingPassword, mTimeSlot.getString(TimeSlot.ID), null), true)
        );
    }

    private void showDetails() {
        OfferDetailsActivity offerDetails = new OfferDetailsActivity();
        offerDetails.setOffer(mOffer, null);
        mParent.presentFragment(offerDetails);
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

        stopCountDown();
    }

    private String fixDigits(int v) {
        if (v < 10) {
            return "0" + v;
        }
        else {
            return String.valueOf(v);
        }
    }

}