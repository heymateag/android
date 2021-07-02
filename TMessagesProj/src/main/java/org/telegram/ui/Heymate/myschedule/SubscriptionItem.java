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
import com.amplifyframework.datastore.generated.model.PurchasedPlan;
import com.yashoid.sequencelayout.SequenceLayout;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.telegram.ui.Heymate.HeymatePayment;
import org.telegram.ui.Heymate.HtOfferDetailsPopUp;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;
import org.telegram.ui.ProfileActivity;

import works.heymate.core.Texts;
import works.heymate.core.offer.PurchasePlanTypes;

public class SubscriptionItem extends SequenceLayout implements View.OnClickListener {

    private static final String TAG = "SubscriptionItem";

    private final BaseFragment mParent;

    private final ImageView mImageUser;
    private final TextView mTextName;
    private final TextView mTextInfo;
    private final TextView mTextMoreInfo;
    private final TextView mButtonLeft;
    private final TextView mButtonRight;

    private PurchasedPlan mPurchasedPlan = null;
    private Offer mOffer = null;

    private int mUserId = 0;

    private ImageReceiver avatarImage = new ImageReceiver(this);
    private AvatarDrawable avatarDrawable = new AvatarDrawable();

    public SubscriptionItem(Context context, BaseFragment parent) {
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
        mTextMoreInfo = findViewById(works.heymate.beta.R.id.text_more_info);
        mButtonLeft = findViewById(works.heymate.beta.R.id.button_left);
        mButtonRight = findViewById(works.heymate.beta.R.id.button_right);

        mTextName.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        mTextMoreInfo.setVisibility(VISIBLE);
        mTextMoreInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mButtonLeft.setText(Texts.get(Texts.DETAILS));
        mButtonLeft.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText3));
        mButtonLeft.setBackground(Theme.createBorderRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_divider)));

        mImageUser.setOnClickListener(this);
        mTextName.setOnClickListener(this);

        mButtonLeft.setOnClickListener(this);

        avatarImage.setRoundRadius(AndroidUtilities.dp(28));
    }

    public void setPurchasedPlan(PurchasedPlan purchasedPlan) {
        mPurchasedPlan = purchasedPlan;

        try {
            mUserId = Integer.parseInt(purchasedPlan.getServiceProviderId());
        } catch (Throwable t) {
            mUserId = 0;
        }

        updateLayout();
    }

    public String getPurchasedPlanId() {
        return mPurchasedPlan == null ? null : mPurchasedPlan.getId();
    }

    public void setOffer(Offer offer) {
        mOffer = offer;

        try {
            mUserId = Integer.parseInt(offer.getUserId());
        } catch (Throwable t) {
            mUserId = 0;
        }

        updateLayout();
    }

    private void updateLayout() {
        if (mUserId != 0) {
            TLRPC.User user = MessagesController.getInstance(mParent.getCurrentAccount()).getUser(mUserId);
            onUserLoaded(user);
        }
        else {
            onUserLoaded(null);
        }

        if (mOffer != null) {
            mTextInfo.setText(mOffer.getTitle());
        }

        if (mPurchasedPlan != null) {
            if (PurchasePlanTypes.BUNDLE.equals(mPurchasedPlan.getPlanType())) {
                if (mOffer != null) {
                    try {
                        PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(mOffer.getPricingInfo()));

                        int remainingSessions = pricingInfo.bundleCount - mPurchasedPlan.getFinishedReservationsCount() - mPurchasedPlan.getPendingReservationsCount();

                        mTextMoreInfo.setText(remainingSessions + " reservations remaining"); // TODO Texts

                        if (remainingSessions > 0) {
                            mButtonLeft.setVisibility(VISIBLE);
                            setRightPositive();
                            mButtonRight.setText("Schedule");
                            mButtonRight.setOnClickListener(v -> scheduleNewReservation());
                        }
                        else {
                            mButtonLeft.setVisibility(GONE);
                            setRightAsDetails();
                            mButtonRight.setOnClickListener(v -> showDetails());
                        }
                    } catch (JSONException e) {
                        mTextMoreInfo.setText("");
                        mButtonLeft.setVisibility(GONE);
                        mButtonRight.setVisibility(GONE);
                    }
                }
                else {
                    mTextMoreInfo.setText("");
                    mButtonLeft.setVisibility(GONE);
                    mButtonRight.setVisibility(GONE);
                }
            }
            else {
                mTextMoreInfo.setText("Unlimited reservations");

                mButtonLeft.setVisibility(VISIBLE);
                setRightPositive();
                mButtonRight.setText("Schedule");
                mButtonRight.setOnClickListener(v -> scheduleNewReservation());
            }
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
            showDetails();
            return;
        }
    }

    private void scheduleNewReservation() {
        if (mOffer == null || mPurchasedPlan == null) {
            return;
        }

        HeymatePayment.purchaseTimeSlot(mParent, mOffer, mPurchasedPlan, null);
    }

    private void showDetails() {
        HtOfferDetailsPopUp detailsPopUp = new HtOfferDetailsPopUp(getContext(), mParent, mOffer, null);
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
