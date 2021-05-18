package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.amplifyframework.datastore.generated.model.TimeSlot;
import com.google.android.exoplayer2.util.Log;
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
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.HtOfferDetailsPopUp;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.LogToGroup;
import org.telegram.ui.Heymate.MeetingType;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.ProfileActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class MyOfferItem extends SequenceLayout implements View.OnClickListener {

    private static final String TAG = "MyOfferItem";

    private final BaseFragment mParent;

    private final ImageView mImageUser;
    private final TextView mTextName;
    private final TextView mTextInfo;
    private final TextView mButtonLeft;
    private final TextView mButtonRight;

    private boolean mIsOnlineMeeting;

    private TimeSlot mTimeSlot = null;
    private List<Reservation> mReservations = null;
    private Offer mOffer = null;

    private int mUserId = 0;

    private final ImageReceiver avatarImage = new ImageReceiver(this);
    private final AvatarDrawable avatarDrawable = new AvatarDrawable();

    public MyOfferItem(Context context, BaseFragment parent) {
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

    public void setTimeSlot(TimeSlot timeSlot) {
        mTimeSlot = timeSlot;
        mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mTimeSlot.getMeetingType());

        mUserId = 0;

        updateLayout();
    }

    public void setReservations(List<Reservation> reservations) {
        mReservations = reservations;

        if (reservations != null && reservations.size() == 1) {
            try {
                mUserId = Integer.parseInt(reservations.get(0).getConsumerId());
            } catch (Throwable t) {
                mUserId = 0;
            }

            updateLayout();
        }
    }

    public String getTimeSlotId() {
        return mTimeSlot == null ? null : mTimeSlot.getId();
    }

    public void setOffer(Offer offer) {
        mOffer = offer;
        mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mOffer.getMeetingType());

        updateLayout();
    }

    private HtTimeSlotStatus concludeStatus() {
        if (mReservations == null || mReservations.isEmpty()) {
            return null;
        }

        if (mReservations.size() == 1) {
            return HtTimeSlotStatus.valueOf(mReservations.get(0).getStatus());
        }

        HtTimeSlotStatus status = HtTimeSlotStatus.BOOKED;

        for (Reservation reservation: mReservations) {
            HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());

            if (reservationStatus.happensAfter(status)) {
                status = reservationStatus;
            }
        }

        ensureUniformStatus(status, false);

        return status;
    }

    private void updateLayout() {
        if (mUserId != 0) {
            TLRPC.User user = MessagesController.getInstance(mParent.getCurrentAccount()).getUser(mUserId);
            onUserLoaded(user);
        }
        else {
            onUserLoaded(null);
        }

        HtTimeSlotStatus status = concludeStatus();

        if (mOffer != null && mTimeSlot != null && mReservations != null) {
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
                    .replace(MyScheduleUtils.PLACEHOLDER_SUB_CATEGORY, mOffer.getSubCategory())
                    .replace(MyScheduleUtils.PLACEHOLDER_TIME_DIFF, MyScheduleUtils.getTimeDiff(mTimeSlot.getStartTime() * 1000L));

            mTextInfo.setText(text);
        }
        else {
            mTextInfo.setText("");
        }

        if (status != null) {
            switch (status) {
                case BOOKED:
                    mButtonLeft.setVisibility(VISIBLE);
                    enableLeft();
                    setRightPositive();
                    mButtonRight.setText(Texts.get(mIsOnlineMeeting ? Texts.START_SESSION : Texts.START));
                    mButtonRight.setOnClickListener(v -> markAsStarted());
                    break;
                case FINISHED:
                case CANCELLED_BY_CONSUMER:
                case CANCELLED_BY_SERVICE_PROVIDER:
                    mButtonLeft.setVisibility(GONE);
                    setRightAsDetails();
                    break;
                case MARKED_AS_STARTED:
                    mButtonLeft.setVisibility(GONE);
                    setRightAsDetails();
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

        LoadingUtil.onLoadingStarted(getContext());

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        ArrayList<Reservation> remainingReservations = new ArrayList<>(mReservations);

        cancelReservations(remainingReservations, wallet);

        disableLeft();
        disableRight();
    }

    private void cancelReservations(List<Reservation> remainingReservations, Wallet wallet) {
        if (remainingReservations.isEmpty()) {
            LoadingUtil.onLoadingFinished();
            return;
        }

        Reservation reservation = remainingReservations.remove(0);

        wallet.cancelOffer(mOffer, reservation, false, (success, errorCause) -> {
            if (success) {
                HtAmplify.getInstance(getContext()).updateReservation(reservation, HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER);
            }
            else {
                Log.e(TAG, "Failed to cancel offer", errorCause);
                LogToGroup.log("Failed to cancel offer", errorCause, mParent);
            }

            cancelReservations(remainingReservations, wallet);
        });
    }

    private void markAsStarted() {
        ensureUniformStatus(HtTimeSlotStatus.MARKED_AS_STARTED, true);

        disableLeft();
        disableRight();
    }

    private void markAsFinished() {
        ensureUniformStatus(HtTimeSlotStatus.MARKED_AS_FINISHED, true);

        disableLeft();
        disableRight();
    }

    private void ensureUniformStatus(HtTimeSlotStatus status, boolean showLoading) {
        if (mReservations == null) {
            return;
        }

        String meetingId = mIsOnlineMeeting ? getMeetingId() : null;

        if (status == HtTimeSlotStatus.MARKED_AS_STARTED && mIsOnlineMeeting) {
            joinSession(meetingId);
        }

        ArrayList<Reservation> reservations = new ArrayList<>(mReservations);

        if (showLoading) {
            LoadingUtil.onLoadingStarted(getContext());
        }

        ensureNextStatus(reservations, status, meetingId, showLoading);
    }

    private void ensureNextStatus(ArrayList<Reservation> reservations, HtTimeSlotStatus status, String meetingId, boolean hideLoading) {
        if (reservations.isEmpty()) {
            if (hideLoading) {
                LoadingUtil.onLoadingFinished();
            }
            return;
        }

        Reservation reservation = reservations.remove(0);

        HtTimeSlotStatus reservationStatus = HtTimeSlotStatus.valueOf(reservation.getStatus());

        HtTimeSlotStatus statusToApply = status;

        if (statusToApply == HtTimeSlotStatus.STARTED) {
            if (reservationStatus == HtTimeSlotStatus.MARKED_AS_STARTED) {
                ensureNextStatus(reservations, status, meetingId, hideLoading);
                return;
            }

            statusToApply = HtTimeSlotStatus.MARKED_AS_STARTED;
        }
        else if (statusToApply == HtTimeSlotStatus.FINISHED) {
            if (reservationStatus == HtTimeSlotStatus.MARKED_AS_FINISHED) {
                ensureNextStatus(reservations, status, meetingId, hideLoading);
                return;
            }

            statusToApply = HtTimeSlotStatus.MARKED_AS_FINISHED;
        }

        if (statusToApply == reservationStatus) {
            ensureNextStatus(reservations, status, meetingId, hideLoading);
            return;
        }

        HtAmplify.getInstance(getContext()).updateReservation(reservation, statusToApply, meetingId);

        if (statusToApply == HtTimeSlotStatus.MARKED_AS_STARTED) {
            // TODO Send in app message to consumer to join.
        }

        if (statusToApply == HtTimeSlotStatus.CANCELLED_BY_SERVICE_PROVIDER) {
            Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

            wallet.cancelOffer(mOffer, reservation, false, (success, errorCause) -> {
                ensureNextStatus(reservations, status, meetingId, hideLoading);
            });
        }
        else {
            ensureNextStatus(reservations, status, meetingId, hideLoading);
        }
    }

    private void joinSession() {
        joinSession(getMeetingId());
    }

    private void joinSession(String meetingId) {
        // TODO Join session
    }

    private String getMeetingId() {
        if (mReservations == null) {
            return null;
        }

        for (Reservation reservation: mReservations) {
            if (reservation.getMeetingId() != null) {
                return reservation.getMeetingId();
            }
        }

        return UUID.randomUUID().toString();
    }

    private void showDetails() {
        HtOfferDetailsPopUp detailsPopUp = new HtOfferDetailsPopUp(getContext(), mParent,  0, mOffer, null);
        AlertDialog dialog = detailsPopUp.create();
        detailsPopUp.closeImage.setOnClickListener(v -> dialog.dismiss());
        mParent.showDialog(dialog);
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