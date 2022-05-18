package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.log.LogToGroup;
import org.telegram.ui.Heymate.OnlineReservation;
import org.telegram.ui.Heymate.offer.OfferDetailsActivity;
import org.telegram.ui.ProfileActivity;

import works.heymate.api.APIObject;
import works.heymate.api.APIs;
import works.heymate.beta.R;
import works.heymate.model.Offer;
import works.heymate.model.Reservation;

public class MeetingMessageItem extends SequenceLayout {

    private ImageView mImageUser;
    private TextView mTitle;
    private TextView mSubCategory;
    private TextView mDescription;
    private TextView mTimer;
    private TextView mMoreDetails;
    private TextView mJoin;

    private Handler mHandler;

    private BaseFragment mParent = null;

    private APIObject mOffer = null;
    private APIObject mReservation = null;
    private long mUserId;

    private String mLoadingOfferId = null;
    private String mLoadingReservationId = null;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    public MeetingMessageItem(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MeetingMessageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MeetingMessageItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        setWillNotDraw(false);

        LayoutInflater.from(context).inflate(R.layout.item_meetingmessage, this, true);
        addSequences(R.xml.sequences_item_meetingmessage);

        View background = findViewById(R.id.background);
        mImageUser = findViewById(R.id.image_user);
        mTitle = findViewById(R.id.title);
        mSubCategory = findViewById(R.id.subcategory);
        mDescription = findViewById(R.id.description);
        mTimer = findViewById(R.id.timer);
        mMoreDetails = findViewById(R.id.more_details);
        mJoin = findViewById(R.id.join);

        int cornerRadius = AndroidUtilities.dp(8);

        background.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));

        mTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mSubCategory.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mTimer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        mMoreDetails.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));
        mMoreDetails.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mMoreDetails.setTypeface(mMoreDetails.getTypeface(), Typeface.BOLD);
        mMoreDetails.setText("More Details"); // TODO Texts

        mJoin.setBackground(Theme.createRoundRectDrawable(cornerRadius, ContextCompat.getColor(context, R.color.ht_theme)));
        mJoin.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mJoin.setTypeface(mJoin.getTypeface(), Typeface.BOLD);
        mJoin.setText("Join Session");

        mImageUser.setOnClickListener(v -> {
            if (mUserId != 0) {
                Bundle args = new Bundle();
                args.putLong("user_id", mUserId);
                mParent.presentFragment(new ProfileActivity(args));
            }
        });

        mMoreDetails.setOnClickListener(v -> {
            if (mOffer != null) {
                OfferDetailsActivity offerDetails = new OfferDetailsActivity();
                offerDetails.setOffer(mOffer, null);
                mParent.presentFragment(offerDetails);
            }
        });

        mJoin.setOnClickListener(v -> {
            if (mParent != null && userCanJoin()) {
                String meetingId = mReservation.getString(Reservation.MEETING_ID);
                String meetingPassword = mReservation.getString(Reservation.MEETING_PASSWORD);

                if (meetingId != null) {
                    LogToGroup.logIfCrashed(() -> {
                        mParent.presentFragment(new OnlineMeetingActivity(meetingId, meetingPassword, null, mReservation.getString(Reservation.ID)));
                    });
                }
            }
        });

        avatarImage.setRoundRadius(AndroidUtilities.dp(18));

        mHandler = new Handler();
    }

    public void setParent(BaseFragment parent) {
        mParent = parent;
    }

    public void setReservation(APIObject reservation) {
        mReservation = reservation;

        if (!reservation.getString(Reservation.ID).equals(mLoadingReservationId)) {
            mLoadingReservationId = null;
        }

        if (mReservation.getString(Reservation.OFFER_ID) == null || mReservation.getString(Reservation.STATUS) == null) {
            mOffer = null;
            mLoadingOfferId = null;

            mLoadingReservationId = mReservation.getString(Reservation.ID);

            String reservationIdToBeLoaded = mLoadingReservationId;

            APIs.get().getReservation(mLoadingReservationId, result -> {
                if (result.response != null) {
                    if (result.response.getString(Reservation.ID).equals(mLoadingReservationId)) {
                        setReservation(result.response);
                    }
                }

                if (reservationIdToBeLoaded.equals(mLoadingReservationId)) {
                    mLoadingReservationId = null;
                }
            });
        }
        else {
            if (mOffer == null || !mOffer.getString(Offer.ID).equals(mReservation.getString(Reservation.OFFER_ID))) {
                mOffer = null;

                if (!mReservation.getString(Reservation.OFFER_ID).equals(mLoadingOfferId)) {
                    mLoadingOfferId = mReservation.getString(Reservation.OFFER_ID);

                    String offerIdToBeLoaded = mLoadingOfferId;

                    APIs.get().getOffer(mLoadingOfferId, result -> {
                        if (result.response != null) {
                            if (result.response.getString(Offer.ID).equals(mLoadingOfferId)) {
                                setOffer(result.response);
                            }
                        }

                        if (offerIdToBeLoaded.equals(mLoadingOfferId)) {
                            mLoadingOfferId = null;
                        }
                    });
                }
            }
        }

        updateLayout();
    }

    private void setOffer(APIObject offer) {
        mOffer = offer;

        updateLayout();
    }

    private void updateLayout() {
        if (mReservation != null && mOffer != null) {
            OnlineReservation.stabilizeReservationStatus(getContext(), mReservation, mOffer);
        }

        String userId = mReservation == null ? null : mReservation.getString(Reservation.SERVICE_PROVIDER_ID);

        if (userId == null) {
            userId = mOffer == null ? null : mOffer.getString(Offer.USER_ID);
        }

        if (userId != null) {
            try {
                mUserId = Long.parseLong(userId);
                TLRPC.User user = MessagesController.getInstance(UserConfig.selectedAccount).getUser(mUserId);
                avatarDrawable.setInfo(user);
                avatarImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, null, user, 0);
            } catch (NumberFormatException e) {
                userId = null;
                mUserId = 0;
            }
        }

        if (userId == null) {
            TLRPC.User user = null;
            avatarDrawable.setInfo(user);
            avatarImage.setImage(ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, null, user, 0);
        }

        if (mOffer != null) {
            mTitle.setText(mOffer.getString(Offer.TITLE));
            mSubCategory.setText(mOffer.getString(Offer.CATEGORY + "." + Offer.Category.SUB_CATEGORY));
            mDescription.setText(mOffer.getString(Offer.DESCRIPTION));
        }

        if (userCanJoin()) {
            mHandler.post(mTimeTracker);
        }
        else {
            mTimer.setText("");
            mHandler.removeCallbacks(mTimeTracker);
        }
    }

    private final Runnable mTimeTracker = new Runnable() {
        @Override
        public void run() {
            // TODO timer based on start time
//            if (mReservation != null) {
//                Integer startTime = mReservation.getStartTime();
//
//                if (startTime != null) {
//                    int passedSeconds = (int) Math.abs(System.currentTimeMillis() / 1000 - startTime);
//
//                    if (passedSeconds >= 0) {
//                        int passedMinutes = passedSeconds / 60;
//                        passedSeconds %= 60;
//
//                        int passedHours = passedMinutes / 60;
//                        passedMinutes %= 60;
//
//                        mTimer.setText(fixDigits(passedHours) + ":" + fixDigits(passedMinutes) + ":" + fixDigits(passedSeconds));
//                    }
//                }
//
//                if (userCanJoin()) {
//                    mHandler.postDelayed(mTimeTracker, 1000);
//                }
//            }
        }
    };

    private boolean userCanJoin() {
        return mReservation != null && mReservation.getString(Reservation.STATUS) != null && HtTimeSlotStatus.valueOf(mReservation.getString(Reservation.STATUS)).getStateIndex() <= HtTimeSlotStatus.STARTED.getStateIndex();
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

    private String fixDigits(int v) {
        if (v < 10) {
            return "0" + v;
        }
        else {
            return String.valueOf(v);
        }
    }

}
