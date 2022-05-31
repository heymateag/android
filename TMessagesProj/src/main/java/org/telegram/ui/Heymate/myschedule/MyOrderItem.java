package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserObject;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.log.LogToGroup;
import works.heymate.model.MeetingType;
import org.telegram.ui.Heymate.OnlineReservation;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.offer.OfferDetailsActivity;
import org.telegram.ui.Heymate.onlinemeeting.OnlineMeetingActivity;
import org.telegram.ui.ProfileActivity;

import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.celo.CeloError;
import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;
import works.heymate.model.Offer;
import works.heymate.model.Reservation;
import works.heymate.model.User;
import works.heymate.model.Users;

public class MyOrderItem extends SequenceLayout implements View.OnClickListener {

    private static final String TAG = "MyOrderItem";

    private final BaseFragment mParent;

    private final ImageView mImageUser;
    private final TextView mTextName;
    private final TextView mTextInfo;
    private final TextView mButtonLeft;
    private final TextView mButtonRight;

    private boolean mIsOnlineMeeting;

    private APIObject mReservation = null;
    private APIObject mOffer = null;

    private String mUserId = null;
    private long mTelegramId = 0;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    public MyOrderItem(Context context, BaseFragment parent) {
        super(context);
        setWillNotDraw(false);

        mParent = parent;

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

    public void setReservation(APIObject reservation) {
        mReservation = reservation;

        updateIsOnlineMeeting();
        updateLayout();
    }

    public String getReservationId() {
        return mReservation == null ? null : mReservation.getString(Reservation.ID);
    }

    public void setOffer(APIObject offer) {
        mOffer = offer;

        try {
            mUserId = offer.getString(Offer.USER_ID);
        } catch (Throwable t) {
            mUserId = null;
        }

        updateIsOnlineMeeting();
        updateLayout();
    }

    private void updateIsOnlineMeeting() {
        if (mOffer != null) {
            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mOffer.getString(Offer.MEETING_TYPE));
        }
//        else if (mReservation != null) { // TODO meeting type from reservation?
//            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mReservation.getMeetingType());
//        }
        else {
            mIsOnlineMeeting = false;
        }
    }

    private void updateLayout() {
        if (mUserId != null) {
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
                    mTelegramId = 0;
                    onUserLoaded(null);
                }
            });
        }
        else {
            mTelegramId = 0;
            onUserLoaded(null);
        }

        if (mOffer != null && mReservation != null) {
            OnlineReservation.stabilizeReservationStatus(getContext(), mReservation, mOffer);

            String text;

            HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(mReservation.getString(Reservation.STATUS));

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
                    .replace(MyScheduleUtils.PLACEHOLDER_TIME_DIFF, MyScheduleUtils.getTimeDiff(System.currentTimeMillis()));
//                    .replace(MyScheduleUtils.PLACEHOLDER_TIME_DIFF, MyScheduleUtils.getTimeDiff(mReservation.getStartTime() * 1000L)); // TODO reservation start time

            mTextInfo.setText(text);
        }
        else {
            mTextInfo.setText("");
        }

        if (mReservation != null) {
            HtTimeSlotStatus status = HtTimeSlotStatus.valueOf(mReservation.getString(Reservation.STATUS));

            switch (status) {
                case BOOKED:
                    mButtonLeft.setVisibility(VISIBLE);
                    enableLeft();
                    setRightAsDetails();
                    break;
                case FINISHED:
                case CANCELLED_BY_CONSUMER:
                case CANCELLED_BY_SERVICE_PROVIDER:
                    mButtonLeft.setVisibility(GONE);
                    setRightAsDetails();
                    break;
                case MARKED_AS_STARTED:
                    mButtonLeft.setVisibility(VISIBLE);
                    enableLeft();
                    setRightPositive();
                    if (mIsOnlineMeeting) {
                        mButtonRight.setText(Texts.get(Texts.JOIN_SESSION));
                        mButtonRight.setOnClickListener(v -> joinSession());
                    }
                    else {
                        mButtonRight.setText(Texts.get(Texts.CONFIRM));
                        mButtonRight.setOnClickListener(v -> confirmStarted());
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
                        setRightAsDetails();
                    }
                    break;
                case MARKED_AS_FINISHED:
                    mButtonLeft.setVisibility(GONE);
                    if (mIsOnlineMeeting) {
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
        if (mOffer == null || mReservation == null) {
            return;
        }

        AlertDialog loading = new AlertDialog(getContext(), 3);
        loading.setCanCancel(false);
        loading.show();

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        wallet.cancelOffer(mOffer, null, mReservation, true, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            loading.dismiss();

            if (success) {
                APIs.get().updateReservation(mReservation.getString(Reservation.ID), HtTimeSlotStatus.CANCELLED_BY_CONSUMER.name(), result -> {
                    // I don't care!
                });
            }
            else {
                Log.e(TAG, "Failed to cancel offer", errorCause);
                LogToGroup.log("Failed to cancel offer", errorCause);

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

                updateLayout();
            }
        });

        disableLeft();
        disableRight();
    }

    private void confirmStarted() {
        if (mOffer == null || mReservation == null) {
            return;
        }

        LoadingUtil.onLoadingStarted();

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        wallet.startOffer(mOffer, null, mReservation, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {
                APIs.get().updateReservation(mReservation.getString(Reservation.ID), HtTimeSlotStatus.STARTED.name(), result -> {
                    // I don't care!
                });
            }
            else {
                Log.e(TAG, "Failed to confirm started offer", errorCause);
                LogToGroup.log("Failed to confirm started offer", errorCause);

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

                updateLayout();
            }
        });

        disableLeft();
        disableRight();
    }

    private void confirmFinished() {
        if (mOffer == null || mReservation == null) {
            return;
        }

        LoadingUtil.onLoadingStarted();

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        wallet.finishOffer(mOffer, null, mReservation, TG2HM.getDefaultCurrency(), (success, errorCause) -> {
            LoadingUtil.onLoadingFinished();

            if (success) {

                APIs.get().updateReservation(mReservation.getString(Reservation.ID), HtTimeSlotStatus.FINISHED.name(), result -> {
                    // I don't care!
                });
            }
            else {
                Log.e(TAG, "Failed to confirm finished offer", errorCause);
                LogToGroup.log("Failed to confirm finished offer", errorCause);

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

                updateLayout();
            }
        });

        disableLeft();
        disableRight();
    }

    private void joinSession() {
        if (mReservation == null || mReservation.getString(Reservation.MEETING_ID) == null) {
            return;
        }

        APIs.get().updateReservation(mReservation.getString(Reservation.ID), HtTimeSlotStatus.STARTED.name(), result -> {
            // I don't care!
        });

        String meetingId = mReservation.getString(Reservation.MEETING_ID);
        String meetingPassword = mReservation.getString(Reservation.MEETING_PASSWORD);

        LogToGroup.logIfCrashed(() -> {
            mParent.presentFragment(new OnlineMeetingActivity(meetingId, meetingPassword,null, mReservation.getString(Reservation.ID)), true);
        });
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
    }

}
