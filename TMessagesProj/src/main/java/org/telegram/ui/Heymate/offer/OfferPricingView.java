package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;
import com.yashoid.sequencelayout.Span;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RadioButton;

import works.heymate.core.Currency;
import works.heymate.core.Money;

import java.util.List;

import works.heymate.beta.R;
import works.heymate.core.offer.PurchasePlanTypes;
import works.heymate.model.Pricing;

public class OfferPricingView extends SequenceLayout {

    public interface OnPlanChangedListener {

        void onPlanChanged(String plan);

    }

    private OnPlanChangedListener mOnPlanChangedListener = null;

    private RadioButton mSingleRadio;
    private TextView mSingle;
    private TextView mSingleInfo;
    private TextView mSinglePrice;

    private RadioButton mBundleRadio;
    private TextView mBundle;
    private TextView mBundleInfo;
    private TextView mBundleDiscount;
    private TextView mBundlePrice;

    private RadioButton mSubscriptionRadio;
    private TextView mSubscription;
    private TextView mSubscriptionInfo;
    private TextView mSubscriptionPrice;

    private Pricing mPricing = null;
    private boolean mOnlySinglePrice = true;

    private String mSelectedPlan = null;

    public OfferPricingView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferPricingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferPricingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.offer_pricing, this, true);
        addSequences(R.xml.sequences_offer_pricing);

        mSingleRadio = findViewById(R.id.single_radio);
        mSingle = findViewById(R.id.single);
        mSingleInfo = findViewById(R.id.single_info);
        mSinglePrice = findViewById(R.id.single_price);

        mBundleRadio = findViewById(R.id.bundle_radio);
        mBundle = findViewById(R.id.bundle);
        mBundleInfo = findViewById(R.id.bundle_info);
        mBundleDiscount = findViewById(R.id.bundle_discount);
        mBundlePrice = findViewById(R.id.bundle_price);

        mSubscriptionRadio = findViewById(R.id.subscription_radio);
        mSubscription = findViewById(R.id.subscription);
        mSubscriptionInfo = findViewById(R.id.subscription_info);
        mSubscriptionPrice = findViewById(R.id.subscription_price);

        setupRadio(mSingleRadio);
        setupRadio(mBundleRadio);
        setupRadio(mSubscriptionRadio);

        mSingle.setText("Single"); // TODO Texts
        mBundle.setText("Bundle");
        mSubscription.setText("Subscription");

        mSingleInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mBundleInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mSubscriptionInfo.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        mBundleDiscount.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(40), Theme.getColor(Theme.key_wallet_greenText) & 0x33ffffff));
        mBundleDiscount.setTextColor(Theme.getColor(Theme.key_wallet_greenText));

        findViewById(R.id.button_single).setOnClickListener(v -> setSelectedPlan(PurchasePlanTypes.SINGLE, true));
        findViewById(R.id.button_bundle).setOnClickListener(v -> setSelectedPlan(PurchasePlanTypes.BUNDLE, true));
        findViewById(R.id.button_subscription).setOnClickListener(v -> setSelectedPlan(PurchasePlanTypes.SUBSCRIPTION, true));
    }

    public void setBottomPadding(int dp) {
        List<Span> spans = findSequenceById("spine").getSpans();

        spans.get(spans.size() - 1).size = dp;
        requestLayout();
    }

    public void setOnPlanChangedListener(OnPlanChangedListener listener) {
        mOnPlanChangedListener = listener;
    }

    public void setPricingInfo(Pricing pricing, String selectedPlan) {
        mPricing = pricing;

        if (mPricing == null) {
            return;
        }

        mSingleInfo.setText("1 session");
        mSinglePrice.setText(Money.create(mPricing.getPrice() * 100, Currency.forName(mPricing.getCurrency())).toString());

        mOnlySinglePrice = true;

        if (mPricing.getBundleCount() > 0) {
            mOnlySinglePrice = false;

            mBundleRadio.setVisibility(VISIBLE);
            mBundle.setVisibility(VISIBLE);
            mBundleInfo.setVisibility(VISIBLE);
            mBundlePrice.setVisibility(VISIBLE);

            mBundleInfo.setText(mPricing.getBundleCount() + " sessions");
            mBundlePrice.setText(Money.create(mPricing.getBundleTotalPrice() * 100, Currency.forName(mPricing.getCurrency())).toString());

            if (mPricing.getBundleDiscountPercent() > 0) {
                mBundleDiscount.setVisibility(VISIBLE);
                mBundleDiscount.setText(mPricing.getBundleDiscountPercent() + "% off");
            }
            else {
                mBundleDiscount.setVisibility(INVISIBLE);
            }
        }
        else {
            mBundleRadio.setVisibility(GONE);
            mBundle.setVisibility(GONE);
            mBundleInfo.setVisibility(GONE);
            mBundleDiscount.setVisibility(GONE);
            mBundlePrice.setVisibility(GONE);
        }

        if (mPricing.getSubscriptionPeriod() != null) {
            mOnlySinglePrice = false;

            mSubscriptionRadio.setVisibility(VISIBLE);
            mSubscription.setVisibility(VISIBLE);
            mSubscriptionInfo.setVisibility(VISIBLE);
            mSubscriptionPrice.setVisibility(VISIBLE);

            mSubscriptionInfo.setText(mPricing.getSubscriptionPeriod() + " - Unlimited sessions");
            mSubscriptionPrice.setText(Money.create(mPricing.getSubscriptionPrice() * 100, Currency.forName(mPricing.getCurrency())).toString());
        }
        else {
            mSubscriptionRadio.setVisibility(GONE);
            mSubscription.setVisibility(GONE);
            mSubscriptionInfo.setVisibility(GONE);
            mSubscriptionPrice.setVisibility(GONE);
        }

        mSingleRadio.setVisibility(mOnlySinglePrice ? GONE : VISIBLE);

        setSelectedPlan(selectedPlan == null ? PurchasePlanTypes.SINGLE : selectedPlan, selectedPlan == null);
    }

    public void setSelectedPlan(String plan) {
        setSelectedPlan(plan, false);
    }

    private void setSelectedPlan(String plan, boolean notify) {
        mSelectedPlan = plan;

        if (mSelectedPlan == null) {
            return;
        }

        switch (mSelectedPlan) {
            case PurchasePlanTypes.SINGLE:
                mSingleRadio.setChecked(true, true);
                mSingle.setTextColor(Theme.getColor(mOnlySinglePrice ? Theme.key_windowBackgroundWhiteBlackText : Theme.key_windowBackgroundWhiteBlueText));
                mSinglePrice.setTextColor(Theme.getColor(mOnlySinglePrice ? Theme.key_windowBackgroundWhiteBlackText : Theme.key_windowBackgroundWhiteBlueText));
                mBundleRadio.setChecked(false, true);
                mBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mBundlePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSubscriptionRadio.setChecked(false, true);
                mSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSubscriptionPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                break;
            case PurchasePlanTypes.BUNDLE:
                mSingleRadio.setChecked(false, true);
                mSingle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSinglePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mBundleRadio.setChecked(true, true);
                mBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                mBundlePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                mSubscriptionRadio.setChecked(false, true);
                mSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSubscriptionPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                break;
            case PurchasePlanTypes.SUBSCRIPTION:
                mSingleRadio.setChecked(false, true);
                mSingle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSinglePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mBundleRadio.setChecked(false, true);
                mBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mBundlePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
                mSubscriptionRadio.setChecked(true, true);
                mSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                mSubscriptionPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                break;
        }

        if (notify && mOnPlanChangedListener != null) {
            mOnPlanChangedListener.onPlanChanged(plan);
        }
    }

    public String getSelectedPlan() {
        return mSelectedPlan;
    }

    private void setupRadio(RadioButton radio) {
        radio.setSize(AndroidUtilities.dp(20));
        radio.setColor(Theme.getColor(Theme.key_radioBackground), Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton));
    }

}
