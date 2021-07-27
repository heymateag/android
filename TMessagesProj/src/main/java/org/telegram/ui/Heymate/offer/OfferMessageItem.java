package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.amplifyframework.datastore.generated.model.Offer;
import com.yashoid.sequencelayout.SequenceLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ChatActivity;
import org.telegram.ui.Components.RadioButton;
import org.telegram.ui.Components.UndoView;
import org.telegram.ui.DialogsActivity;
import org.telegram.ui.Heymate.FileCache;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.ReferralUtils;
import org.telegram.ui.Heymate.createoffer.PriceInputItem;
import org.telegram.ui.Heymate.log.LogToGroup;
import org.telegram.ui.Heymate.payment.WalletExistence;
import org.telegram.ui.Heymate.payment.PaymentController;
import org.telegram.ui.Heymate.widget.OfferImagePlaceHolderDrawable;
import org.telegram.ui.Heymate.widget.RoundedCornersImageView;

import works.heymate.beta.R;
import works.heymate.core.Texts;
import works.heymate.core.offer.OfferUtils;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;

public class OfferMessageItem extends SequenceLayout {

    private static final int IMAGE_WIDTH_DP = 360 - 16 - 4 - 4 - 8 - 32 - 16; // sequences_item_offermessage

    private RoundedCornersImageView mImage;
    private TextView mTitle;
    private TextView mSubCategory;
    private TextView mDescription;
    private View mHolderFixedPrice;
    private RadioButton mRadioFixedPrice;
    private TextView mTitleFixedPrice;
    private TextView mInfoFixedPrice;
    private TextView mPriceFixedPrice;
    private TextView mPriceInfoFixedPrice;
    private View mHolderBundle;
    private RadioButton mRadioBundle;
    private TextView mTitleBundle;
    private TextView mInfoBundle;
    private TextView mPriceBundle;
    private TextView mPriceInfoBundle;
    private View mHolderSubscription;
    private RadioButton mRadioSubscription;
    private TextView mTitleSubscription;
    private TextView mInfoSubscription;
    private TextView mPriceSubscription;
    private TextView mPriceInfoSubscription;
    private TextView mMoreDetails;
    private TextView mBook;
    private ImageView mShare;
    private ImageView mForward;

    private RadioButton mSelectedPurchasePlan = null;

    private BaseFragment mParent;

    private Offer mOffer = null;
    private boolean mFullyLoaded = false;

    private OfferUtils.PhraseInfo mPhraseInfo = null;

    public OfferMessageItem(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferMessageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferMessageItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.item_offermessage, this, true);
        addSequences(R.xml.sequences_item_offermessage);

        View background = findViewById(R.id.background);
        mImage = findViewById(R.id.image);
        mTitle = findViewById(R.id.title);
        mSubCategory = findViewById(R.id.subcategory);
        mDescription = findViewById(R.id.description);
        mHolderFixedPrice = findViewById(R.id.holder_fixed_price);
        mRadioFixedPrice = findViewById(R.id.radio_fixed_price);
        mTitleFixedPrice = findViewById(R.id.title_fixed_price);
        mInfoFixedPrice = findViewById(R.id.info_fixed_price);
        mPriceFixedPrice = findViewById(R.id.price_fixed_price);
        mPriceInfoFixedPrice = findViewById(R.id.price_info_fixed_price);
        mHolderBundle = findViewById(R.id.holder_bundle);
        mRadioBundle = findViewById(R.id.radio_bundle);
        mTitleBundle = findViewById(R.id.title_bundle);
        mInfoBundle = findViewById(R.id.info_bundle);
        mPriceBundle = findViewById(R.id.price_bundle);
        mPriceInfoBundle = findViewById(R.id.price_info_bundle);
        mHolderSubscription = findViewById(R.id.holder_subscription);
        mRadioSubscription = findViewById(R.id.radio_subscription);
        mTitleSubscription = findViewById(R.id.title_subscription);
        mInfoSubscription = findViewById(R.id.info_subscription);
        mPriceSubscription = findViewById(R.id.price_subscription);
        mPriceInfoSubscription = findViewById(R.id.price_info_subscription);
        mMoreDetails = findViewById(R.id.more_details);
        mBook = findViewById(R.id.book);
        mShare = findViewById(R.id.share);
        mForward = findViewById(R.id.forward);

        int cornerRadius = AndroidUtilities.dp(8);

        background.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));

        mImage.setCornerRadius(cornerRadius);
        mTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mSubCategory.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        mRadioFixedPrice.setSize(AndroidUtilities.dp(20));
        mRadioFixedPrice.setColor(Theme.getColor(Theme.key_radioBackground), ContextCompat.getColor(context, R.color.ht_theme));
        mTitleFixedPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTitleFixedPrice.setText("Single"); // TODO Texts
        mInfoFixedPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mInfoFixedPrice.setText("Fixed price");
        mPriceFixedPrice.setTextColor(ContextCompat.getColor(context, R.color.ht_theme));
        mPriceInfoFixedPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mRadioBundle.setSize(AndroidUtilities.dp(20));
        mRadioBundle.setColor(Theme.getColor(Theme.key_radioBackground), ContextCompat.getColor(context, R.color.ht_theme));
        mTitleBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTitleBundle.setText("Bundle");
        mInfoBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mPriceBundle.setTextColor(ContextCompat.getColor(context, R.color.ht_theme));
        mPriceInfoBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mRadioSubscription.setSize(AndroidUtilities.dp(20));
        mRadioSubscription.setColor(Theme.getColor(Theme.key_radioBackground), ContextCompat.getColor(context, R.color.ht_theme));
        mTitleSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTitleSubscription.setText("Subscription");
        mInfoSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mInfoSubscription.setText("Unlimited sessions");
        mPriceSubscription.setTextColor(ContextCompat.getColor(context, R.color.ht_theme));
        mPriceInfoSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));

        mMoreDetails.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));
        mMoreDetails.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mMoreDetails.setTypeface(mMoreDetails.getTypeface(), Typeface.BOLD);
        mMoreDetails.setText("More Details");

        mBook.setBackground(Theme.createRoundRectDrawable(cornerRadius, ContextCompat.getColor(context, R.color.ht_theme)));
        mBook.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mBook.setTypeface(mBook.getTypeface(), Typeface.BOLD);
        mBook.setText("Book Now");

        mShare.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(32), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText)));
        Drawable shareDrawable = AppCompatResources.getDrawable(context, R.drawable.share).mutate();
        shareDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhite), PorterDuff.Mode.SRC_IN);
        mShare.setImageDrawable(shareDrawable);

        mForward.setBackground(Theme.createCircleDrawable(AndroidUtilities.dp(32), Theme.getColor(Theme.key_windowBackgroundWhiteGrayText)));
        Drawable forwardDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_ab_forward).mutate();
        forwardDrawable.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhite), PorterDuff.Mode.SRC_IN);
        mForward.setImageDrawable(forwardDrawable);

        mHolderFixedPrice.setOnClickListener(v -> setSelectedPurchasePlan(mRadioFixedPrice));
        mHolderBundle.setOnClickListener(v -> setSelectedPurchasePlan(mRadioBundle));
        mHolderSubscription.setOnClickListener(v -> setSelectedPurchasePlan(mRadioSubscription));

        mMoreDetails.setOnClickListener(v -> {
            if (mOffer == null || !mFullyLoaded || mParent == null) {
                return;
            }

            OfferDetailsActivity offerDetails = new OfferDetailsActivity();
            offerDetails.setOffer(mOffer, mPhraseInfo);
            mParent.presentFragment(offerDetails);
        });

        mBook.setOnClickListener(v -> initPayment());

        mShare.setOnClickListener(v-> WalletExistence.ensure(() -> promote(true)));
        mForward.setOnClickListener(v-> WalletExistence.ensure(() -> promote(false)));
    }

    private void setSelectedPurchasePlan(RadioButton radio) {
        if (mSelectedPurchasePlan != null) {
            mSelectedPurchasePlan.setChecked(false, true);
        }

        mSelectedPurchasePlan = radio;

        if (mSelectedPurchasePlan != null) {
            mSelectedPurchasePlan.setChecked(true, true);
        }
    }

    public void setParent(BaseFragment parent) {
        mParent = parent;
    }

    public void setPhraseInfo(OfferUtils.PhraseInfo phraseInfo) {
        mPhraseInfo = phraseInfo;
    }

    public void setOffer(Offer offer, boolean fullyLoaded) {
        mOffer = offer;
        mFullyLoaded = fullyLoaded;

        if (fullyLoaded) {
            if (offer != null && offer.getHasImage() != null && offer.getHasImage()) {
                mImage.setVisibility(VISIBLE);
                mImage.setImageDrawable(null);

                String offerId = offer.getId();
                int size = AndroidUtilities.dp(IMAGE_WIDTH_DP);

                FileCache.get().getImage(offerId, size, (success, drawable, exception) -> {
                    if (mOffer == null || !mOffer.getId().equals(offerId)) {
                        return;
                    }

                    if (drawable != null) {
                        mImage.setImageDrawable(drawable);
                    }
                    else {
                        mImage.setImageDrawable(new OfferImagePlaceHolderDrawable(false));
                    }
                });
            }
            else {
                mImage.setImageDrawable(new OfferImagePlaceHolderDrawable(false));
            }
        }
        else {
            mImage.setImageDrawable(new OfferImagePlaceHolderDrawable(false));
        }

        mTitle.setText(offer.getTitle());
        mSubCategory.setText(offer.getSubCategory());
        mDescription.setText(offer.getDescription());

        setSelectedPurchasePlan(mRadioFixedPrice);

        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(offer.getPricingInfo()));

            mPriceFixedPrice.setText(pricingInfo.price + " " + pricingInfo.currency);
            mPriceInfoFixedPrice.setText(pricingInfo.rateType);

            if (pricingInfo.bundleCount > 0) {
                mHolderBundle.setVisibility(VISIBLE);
                mRadioBundle.setVisibility(VISIBLE);
                mTitleBundle.setVisibility(VISIBLE);
                mInfoBundle.setVisibility(VISIBLE);
                mPriceBundle.setVisibility(VISIBLE);
                mPriceInfoBundle.setVisibility(VISIBLE);

                mInfoBundle.setText(pricingInfo.bundleDiscountPercent + "% Off");
                mPriceBundle.setText(pricingInfo.getBundleTotalPrice() + " " + pricingInfo.currency);
                mPriceInfoBundle.setText("per " + pricingInfo.bundleCount + " reservations");
            }
            else {
                mHolderBundle.setVisibility(GONE);
                mRadioBundle.setVisibility(GONE);
                mTitleBundle.setVisibility(GONE);
                mInfoBundle.setVisibility(GONE);
                mPriceBundle.setVisibility(GONE);
                mPriceInfoBundle.setVisibility(GONE);
            }

            if (pricingInfo.subscriptionPeriod != null) {
                mHolderSubscription.setVisibility(VISIBLE);
                mRadioSubscription.setVisibility(VISIBLE);
                mTitleSubscription.setVisibility(VISIBLE);
                mInfoSubscription.setVisibility(VISIBLE);
                mPriceSubscription.setVisibility(VISIBLE);
                mPriceInfoSubscription.setVisibility(VISIBLE);

                mPriceSubscription.setText(pricingInfo.subscriptionPrice + " " + pricingInfo.currency);
                mPriceInfoSubscription.setText(pricingInfo.subscriptionPeriod.toLowerCase()); // TODO Cheating?
            }
            else {
                mHolderSubscription.setVisibility(GONE);
                mRadioSubscription.setVisibility(GONE);
                mTitleSubscription.setVisibility(GONE);
                mInfoSubscription.setVisibility(GONE);
                mPriceSubscription.setVisibility(GONE);
                mPriceInfoSubscription.setVisibility(GONE);
            }

            mRadioFixedPrice.setVisibility((pricingInfo.bundleCount == 0 && pricingInfo.subscriptionPeriod == null) ? GONE : VISIBLE);
        } catch (JSONException | NullPointerException e) { }
    }

    private void promote(boolean share) {
        if (mOffer == null) {
            return;
        }

        if (mPhraseInfo == null || mOffer.getUserId() != null && mOffer.getUserId().equals(String.valueOf(UserConfig.getInstance(UserConfig.selectedAccount).clientUserId))) {
            doPromote(null, share);
            return;
        }

        LoadingUtil.onLoadingStarted();

        ReferralUtils.getReferralId(mPhraseInfo, (success, referralId, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (!success) {
                // TODO Organize error messages
                Toast.makeText(getContext(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
                return;
            }

            doPromote(referralId, share);
        });
    }

    private void doPromote(String referralId, boolean share) {
        String message = OfferUtils.serializeBeautiful(mOffer, referralId, mOffer.getUserId(), OfferUtils.CATEGORY, OfferUtils.EXPIRY);

        if (share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");

            shareIntent.putExtra(Intent.EXTRA_TEXT, message);
            getContext().startActivity(Intent.createChooser(shareIntent, LocaleController.getString("HtPromoteYourOffer", works.heymate.beta.R.string.HtPromoteYourOffer)));
        }
        else {
            Bundle args = new Bundle();
            args.putBoolean("onlySelect", true);
            args.putInt("dialogsType", 3);
            args.putInt("messagesCount", 1);
            args.putInt("hasPoll", 0);
            DialogsActivity fragment = new DialogsActivity(args);
            fragment.setDelegate((fragment1, dids, message1, param) -> {
                if (dids.size() > 1 || dids.get(0) == mParent.getUserConfig().getClientUserId()) {
                    for (int a = 0; a < dids.size(); a++) {
                        long did = dids.get(a);
                        SendMessagesHelper.getInstance(mParent.getCurrentAccount()).sendMessage(message, did, null, null, null, false, null, null, null, true, 0);
                    }
                    fragment1.finishFragment();

                    UndoView undoView = findUndoView();

                    if (undoView != null) {
                        if (dids.size() == 1) {
                            undoView.showWithAction(dids.get(0), UndoView.ACTION_FWD_MESSAGES, 1);
                        } else {
                            undoView.showWithAction(0, UndoView.ACTION_FWD_MESSAGES, 1, dids.size(), null, null);
                        }
                    }
                } else {
                    long did = dids.get(0);

                    SendMessagesHelper.getInstance(mParent.getCurrentAccount()).sendMessage(message, did, null, null, null, false, null, null, null, true, 0);

                    ChatActivity chatActivity = (mParent instanceof ChatActivity) ? (ChatActivity) mParent : null;

                    if (chatActivity == null || did != chatActivity.getDialogId() || chatActivity.getChatMode() == ChatActivity.MODE_PINNED) {
                        int lower_part = (int) did;
                        int high_part = (int) (did >> 32);
                        Bundle args1 = new Bundle();
                        // args1.putBoolean("scrollToTopOnResume", scrollToTopOnResume);
                        if (lower_part != 0) {
                            if (lower_part > 0) {
                                args1.putInt("user_id", lower_part);
                            } else {
                                args1.putInt("chat_id", -lower_part);
                            }
                        } else {
                            args1.putInt("enc_id", high_part);
                        }
                        if (lower_part != 0) {
                            if (!mParent.getMessagesController().checkCanOpenChat(args1, fragment1)) {
                                return;
                            }
                        }
                        if (mParent.presentFragment(new ChatActivity(args1), true)) {
                            if (!AndroidUtilities.isTablet()) {
                                mParent.removeSelfFromStack();
                            }
                        } else {
                            fragment1.finishFragment();
                        }
                    } else {
                        fragment1.finishFragment();
                    }
                }
            });

            mParent.presentFragment(fragment);
        }
    }

    private UndoView findUndoView() {
        if (mParent == null) {
            return null;
        }

        View view = mParent.getFragmentView();

        if (!(view instanceof ViewGroup)) {
            return null;
        }

        ViewGroup parent = (ViewGroup) view;

        for (int i = parent.getChildCount() - 1; i >= 0; i--) {
            if (parent.getChildAt(i) instanceof UndoView) {
                return (UndoView) parent.getChildAt(i);
            }
        }

        return null;
    }

    private void initPayment() {
        if (mSelectedPurchasePlan == null || mOffer == null || !mFullyLoaded) {
            return;
        }

        PurchasePlanInfo purchasePlanInfo;

        try {
            PriceInputItem.PricingInfo pricingInfo = new PriceInputItem.PricingInfo(new JSONObject(mOffer.getPricingInfo()));

            if (mSelectedPurchasePlan == mRadioFixedPrice) {
                purchasePlanInfo = pricingInfo.getPurchasePlanInfo(PurchasePlanTypes.SINGLE);
            }
            else if (mSelectedPurchasePlan == mRadioBundle) {
                purchasePlanInfo = pricingInfo.getPurchasePlanInfo(PurchasePlanTypes.BUNDLE);
            }
            else {
                purchasePlanInfo = pricingInfo.getPurchasePlanInfo(PurchasePlanTypes.SUBSCRIPTION);
            }
        } catch (JSONException e) {
            return;
        }

        PaymentController.get(getContext()).initPayment(mOffer.getId(), purchasePlanInfo.type, mPhraseInfo == null ? null : mPhraseInfo.referralId);
    }

}
