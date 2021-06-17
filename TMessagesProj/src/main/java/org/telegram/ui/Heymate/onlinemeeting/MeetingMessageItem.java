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

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
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
import org.telegram.ui.Heymate.HtAmplify;
import org.telegram.ui.Heymate.HtOfferDetailsPopUp;
import org.telegram.ui.Heymate.HtTimeSlotStatus;
import org.telegram.ui.Heymate.OnlineReservation;
import org.telegram.ui.ProfileActivity;

import works.heymate.beta.R;

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

    private Offer mOffer = null;
    private Reservation mReservation = null;
    private int mUserId;

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
                args.putInt("user_id", mUserId);
                mParent.presentFragment(new ProfileActivity(args));
            }
        });

        mMoreDetails.setOnClickListener(v -> {
            if (mOffer != null) {
                new HtOfferDetailsPopUp(getContext(), mParent, mOffer, null).show();
            }
        });

        mJoin.setOnClickListener(v -> {
            if (mParent != null && userCanJoin()) {
                String meetingId = mReservation.getMeetingId();

                if (meetingId != null) {
                    mParent.presentFragment(new OnlineMeetingActivity(meetingId, null, mReservation.getId()));
                }
            }
        });

        avatarImage.setRoundRadius(AndroidUtilities.dp(18));

        mHandler = new Handler();
    }

    public void setParent(BaseFragment parent) {
        mParent = parent;
    }

    public void setReservation(Reservation reservation) {
        mReservation = reservation;

        if (!reservation.getId().equals(mLoadingReservationId)) {
            mLoadingReservationId = null;
        }

        if (mReservation.getOfferId() == null || mReservation.getStatus() == null) {
            mOffer = null;
            mLoadingOfferId = null;

            mLoadingReservationId = mReservation.getId();

            String reservationIdToBeLoaded = mLoadingReservationId;

            HtAmplify.getInstance(getContext()).getReservation(mLoadingReservationId, (success, result, exception) -> {
                if (success && result != null) {
                    if (result.getId().equals(mLoadingReservationId)) {
                        setReservation(result);
                    }
                }

                if (reservationIdToBeLoaded.equals(mLoadingReservationId)) {
                    mLoadingReservationId = null;
                }
            });
        }
        else {
            if (mOffer == null || !mOffer.getId().equals(mReservation.getOfferId())) {
                mOffer = null;

                if (!mReservation.getOfferId().equals(mLoadingOfferId)) {
                    mLoadingOfferId = mReservation.getOfferId();

                    String offerIdToBeLoaded = mLoadingOfferId;

                    HtAmplify.getInstance(getContext()).getOffer(mReservation.getOfferId(), (success, data, exception) -> {
                        if (success) {
                            if (data.getId().equals(mLoadingOfferId)) {
                                setOffer(data);
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

    private void setOffer(Offer offer) {
        mOffer = offer;

        updateLayout();
    }

    private void updateLayout() {
        if (mReservation != null && mOffer != null) {
            OnlineReservation.stabilizeReservationStatus(getContext(), mReservation, mOffer);
        }

        String userId = mReservation == null ? null : mReservation.getServiceProviderId();

        if (userId == null) {
            userId = mOffer == null ? null : mOffer.getUserId();
        }

        if (userId != null) {
            try {
                mUserId = Integer.parseInt(userId);
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
            mTitle.setText(mOffer.getTitle());
            mSubCategory.setText(mOffer.getSubCategory());
            mDescription.setText(mOffer.getDescription());
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
            if (mReservation != null) {
                Integer startTime = mReservation.getStartTime();

                if (startTime != null) {
                    int passedSeconds = (int) Math.abs(System.currentTimeMillis() / 1000 - startTime);

                    if (passedSeconds >= 0) {
                        int passedMinutes = passedSeconds / 60;
                        passedSeconds %= 60;

                        int passedHours = passedMinutes / 60;
                        passedMinutes %= 60;

                        mTimer.setText(fixDigits(passedHours) + ":" + fixDigits(passedMinutes) + ":" + fixDigits(passedSeconds));
                    }
                }

                if (userCanJoin()) {
                    mHandler.postDelayed(mTimeTracker, 1000);
                }
            }
        }
    };

    private boolean userCanJoin() {
        return mReservation != null && mReservation.getStatus() != null && HtTimeSlotStatus.valueOf(mReservation.getStatus()).getStateIndex() <= HtTimeSlotStatus.STARTED.getStateIndex();
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
