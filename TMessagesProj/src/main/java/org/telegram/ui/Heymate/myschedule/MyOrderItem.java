package org.telegram.ui.Heymate.myschedule;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.HtOfferDetailsPopUp;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.LogToGroup;
import org.telegram.ui.Heymate.MeetingType;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.onlinemeeting.OnlineMeetingActivity;
import org.telegram.ui.ProfileActivity;

import works.heymate.celo.CeloError;
import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;

public class MyOrderItem extends SequenceLayout implements View.OnClickListener {

    private static final String TAG = "MyOrderItem";

    private final BaseFragment mParent;

    private final ImageView mImageUser;
    private final TextView mTextName;
    private final TextView mTextInfo;
    private final TextView mButtonLeft;
    private final TextView mButtonRight;

    private boolean mIsOnlineMeeting;

    private Reservation mReservation = null;
    private Offer mOffer = null;

    private int mUserId = 0;

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

    public void setReservation(Reservation reservation) {
        mReservation = reservation;

        updateIsOnlineMeeting();
        updateLayout();
    }

    public String getReservationId() {
        return mReservation == null ? null : mReservation.getId();
    }

    public void setOffer(Offer offer) {
        mOffer = offer;

        try {
            mUserId = Integer.parseInt(offer.getUserId());
        } catch (Throwable t) {
            mUserId = 0;
        }

        updateIsOnlineMeeting();
        updateLayout();
    }

    private void updateIsOnlineMeeting() {
        if (mReservation != null) {
            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mReservation.getMeetingType());
        }
        else if (mOffer != null) {
            mIsOnlineMeeting = MeetingType.ONLINE_MEETING.equals(mOffer.getMeetingType());
        }
        else {
            mIsOnlineMeeting = false;
        }

        mTextName.setTextColor(mIsOnlineMeeting ? 0xffff0000 : 0xff000000);
    }

    private void updateLayout() {
        if (mUserId != 0) {
            TLRPC.User user = MessagesController.getInstance(mParent.getCurrentAccount()).getUser(mUserId);
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
                    .replace(MyScheduleUtils.PLACEHOLDER_TIME_DIFF, MyScheduleUtils.getTimeDiff(mReservation.getStartTime() * 1000L));

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
        if (mOffer == null || mReservation == null) {
            return;
        }

        AlertDialog loading = new AlertDialog(getContext(), 3);
        loading.setCanCacnel(false);
        loading.show();

        Wallet wallet = Wallet.get(getContext(), TG2HM.getCurrentPhoneNumber());

        wallet.cancelOffer(mOffer, mReservation, true, (success, errorCause) -> {
            loading.dismiss();

            if (success) {
                HtAmplify.getInstance(getContext()).updateReservation(mReservation, HtTimeSlotStatus.CANCELLED_BY_CONSUMER);
            }
            else {
                Log.e(TAG, "Failed to cancel offer", errorCause);
                LogToGroup.log("Failed to cancel offer", errorCause, mParent);

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

        disableLeft();
        disableRight();
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
                HtAmplify.getInstance(getContext()).updateReservation(mReservation, HtTimeSlotStatus.STARTED);
            }
            else {
                Log.e(TAG, "Failed to confirm started offer", errorCause);
                LogToGroup.log("Failed to confirm started offer", errorCause, mParent);

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

        disableLeft();
        disableRight();
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
                HtAmplify.getInstance(getContext()).updateReservation(mReservation, HtTimeSlotStatus.FINISHED);
            }
            else {
                Log.e(TAG, "Failed to confirm finished offer", errorCause);
                LogToGroup.log("Failed to confirm finished offer", errorCause, mParent);

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

        disableLeft();
        disableRight();
    }

    private void joinSession() {
        if (mReservation == null || mReservation.getMeetingId() == null) {
            return;
        }

        HtAmplify.getInstance(getContext()).updateReservation(mReservation, HtTimeSlotStatus.STARTED);

        String meetingId = mReservation.getMeetingId();

        mParent.presentFragment(new OnlineMeetingActivity(meetingId), true);
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
