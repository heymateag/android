package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.MetricAffectingSpan;
import android.text.style.ReplacementSpan;
import android.util.StateSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.SequenceLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.widget.MultiChoicePopup;

import works.heymate.beta.R;
import works.heymate.core.Utils;
import works.heymate.core.offer.PurchasePlanInfo;
import works.heymate.core.offer.PurchasePlanTypes;

public class PriceInputItem extends ExpandableItem {

    private static final String[] CURRENCIES = { "US$", "€" };
    private static final String[] RATE_TYPES = { "Per Session", "Per Hour" };
    public static final String[] SUBSCRIPTION_PERIODS = { "Per month", "Per year" };

    private static final String PRICE = "price";
    private static final String CURRENCY = "currency";
    private static final String RATE_TYPE = "rate_type";
    private static final String BUNDLE_COUNT = "bundle_count";
    private static final String BUNDLE_DISCOUNT_PERCENT = "bundle_discount_percent";
    private static final String SUBSCRIPTION_PERIOD = "subscription_period";
    private static final String SUBSCRIPTION_PRICE = "subscription_price";

    public static class PricingInfo {

        public final int price;
        public final String currency;
        public final String rateType;
        public final int bundleCount;
        public final int bundleDiscountPercent;
        public final String subscriptionPeriod;
        public final int subscriptionPrice;

        public PricingInfo(int price, String currency, String rateType, int bundleCount, int bundleDiscountPercent, String subscriptionPeriod, int subscriptionPrice) {
            this.price = price;
            this.currency = currency;
            this.rateType = rateType;
            this.bundleCount = bundleCount;
            this.bundleDiscountPercent = bundleDiscountPercent;
            this.subscriptionPeriod = subscriptionPeriod;
            this.subscriptionPrice = subscriptionPrice;
        }

        public PricingInfo(JSONObject json) throws JSONException {
            price = json.getInt(PRICE);
            currency = json.getString(CURRENCY);
            rateType = json.getString(RATE_TYPE);

            int bundleCountTemp = 0;
            int bundleDiscountPercentTemp = 0;
            try {
                bundleCountTemp = json.getInt(BUNDLE_COUNT);
                bundleDiscountPercentTemp = json.getInt(BUNDLE_DISCOUNT_PERCENT);
            } catch (JSONException e) { }
            bundleCount = bundleCountTemp;
            bundleDiscountPercent = bundleDiscountPercentTemp;

            String subscriptionPeriodTemp = null;
            int subscriptionPriceTemp = 0;
            try {
                subscriptionPeriodTemp = Utils.getOrNull(json, SUBSCRIPTION_PERIOD);
                subscriptionPriceTemp = json.getInt(SUBSCRIPTION_PRICE);
            } catch (JSONException e) { }
            subscriptionPeriod = subscriptionPeriodTemp;
            subscriptionPrice = subscriptionPriceTemp;
        }

        public int getBundleTotalPrice() {
            return price * bundleCount * (100 - bundleDiscountPercent) / 100;
        }

        public PurchasePlanInfo getPurchasePlanInfo(String purchasePlanType) {
            switch (purchasePlanType) {
                case PurchasePlanTypes.SINGLE:
                    return new PurchasePlanInfo(PurchasePlanTypes.SINGLE, price);
                case PurchasePlanTypes.BUNDLE:
                    return bundleCount == 0 ? null : new PurchasePlanInfo(PurchasePlanTypes.BUNDLE, getBundleTotalPrice());
                case PurchasePlanTypes.SUBSCRIPTION:
                    return subscriptionPeriod == null ? null : new PurchasePlanInfo(PurchasePlanTypes.SUBSCRIPTION, subscriptionPrice);
            }

            throw new RuntimeException("Unknown purchase plan type.");
        }

        public JSONObject asJSON() {
            JSONObject json = new JSONObject();

            try {
                json.put(PRICE, price);
                json.put(CURRENCY, currency);
                json.put(RATE_TYPE, rateType);
                json.put(BUNDLE_COUNT, bundleCount);
                json.put(BUNDLE_DISCOUNT_PERCENT, bundleDiscountPercent);
                json.put(SUBSCRIPTION_PERIOD, subscriptionPeriod);
                json.put(SUBSCRIPTION_PRICE, subscriptionPrice);
            } catch (JSONException e) { }

            return json;
        }

    }

    private EditText mFixedPrice;
    private TextView mCurrency;
    private TextView mRateType;
    private AppCompatCheckBox mCheckBundle;
    private EditText mBundleSessionCount;
    private EditText mBundleDiscountPercent;
    private TextView mBundleCalculatedPrice;
    private AppCompatCheckBox mCheckSubscription;
    private TextView mSubscriptionPeriod;
    private EditText mSubscriptionPrice;
    private TextView mSubscriptionPer;

    public PriceInputItem(@NonNull Context context) {
        super(context);
        setTitle("Pricing");
        setIcon(ContextCompat.getDrawable(context, R.drawable.ic_pricing));
    }

    @Override
    protected View createContent() {
        FrameLayout frame = new FrameLayout(getContext());

        SequenceLayout content = (SequenceLayout) LayoutInflater.from(getContext()).inflate(R.layout.item_priceinput, null, false);

        content.findSequenceById("anchor").getSpans().get(0).size = AndroidUtilities.dp(HEADER_LEFT_MARGIN);

        TextView fixedPriceTitle = content.findViewById(R.id.fixed_price_title);
        mFixedPrice = content.findViewById(R.id.fixed_price);
        mCurrency = content.findViewById(R.id.currency);
        mRateType = content.findViewById(R.id.price_type);
        mCheckBundle = content.findViewById(R.id.check_bundle);
        mBundleSessionCount = content.findViewById(R.id.bundle_session_count);
        TextView bundleSessions = content.findViewById(R.id.bundle_sessions);
        mBundleDiscountPercent = content.findViewById(R.id.bundle_discount_percent);
        TextView bundleDiscount = content.findViewById(R.id.bundle_discount);
        TextView bundlePrice = content.findViewById(R.id.bundle_price);
        mBundleCalculatedPrice = content.findViewById(R.id.bundle_calculated_price);
        mCheckSubscription = content.findViewById(R.id.check_subscription);
        mSubscriptionPeriod = content.findViewById(R.id.subscription_period);
        TextView subscriptionsUnlimited = content.findViewById(R.id.subscriptions_unlimited);
        TextView subscriptionAt = content.findViewById(R.id.subscription_at);
        mSubscriptionPrice = content.findViewById(R.id.subscription_price);
        mSubscriptionPer = content.findViewById(R.id.subscription_per);

        fixedPriceTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        fixedPriceTitle.setText("Fixed Price"); // TODO Texts

        styleInput(mFixedPrice, null, null);
        mFixedPrice.addTextChangedListener(new BaseTextWatcher(this::updateBundleCalculatedPrice));

        styleMultiChoice(mCurrency, CURRENCIES, 1, this::updateBundleCalculatedPrice);
        styleMultiChoice(mRateType, RATE_TYPES, 0, null);

        mCheckBundle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mCheckBundle.setText("Bundle");
        mCheckBundle.setChecked(false);
        mCheckBundle.setOnCheckedChangeListener((buttonView, isChecked) -> updateBundleViews());

        bundleSessions.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        bundleSessions.setText("Sessions");

        styleInput(mBundleSessionCount, bundleSessions, null);
        mBundleSessionCount.addTextChangedListener(new BaseTextWatcher(this::updateBundleCalculatedPrice));

        bundleDiscount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        bundleDiscount.setText("Discount");

        styleInput(mBundleDiscountPercent, bundleDiscount, "%");
        mBundleDiscountPercent.addTextChangedListener(new BaseTextWatcher(this::updateBundleCalculatedPrice));

        bundlePrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        bundlePrice.setText("Total price");

        mBundleCalculatedPrice.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        updateBundleCalculatedPrice();
        updateBundleViews();

        mCheckSubscription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mCheckSubscription.setText("Subscription");
        mCheckSubscription.setChecked(false);
        mCheckSubscription.setOnCheckedChangeListener((buttonView, isChecked) -> updateSubscriptionViews());

        styleMultiChoice(mSubscriptionPeriod, SUBSCRIPTION_PERIODS, 0, null);

        subscriptionsUnlimited.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        subscriptionsUnlimited.setText("unlimited sessions");

        subscriptionAt.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        subscriptionAt.setText("At");

        styleInput(mSubscriptionPrice, null, null);

        mSubscriptionPer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        updateSubscriptionPer();
        updateSubscriptionViews();

        frame.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        return frame;
    }

    private void styleInput(TextView input, View neighbor, String suffix) {
        int enabledColor = Theme.getColor(Theme.key_windowBackgroundWhiteBlackText);
        int disabledColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2);

        input.setTextColor(getColorStateList(enabledColor, disabledColor));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            input.setBackgroundTintList(ColorStateList.valueOf(Theme.getColor(Theme.key_windowBackgroundWhiteHintText)));
        }

        if (neighbor != null) {
            neighbor.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            input.setPadding(
                    input.getPaddingLeft(),
                    input.getPaddingTop(),
                    input.getPaddingRight() + neighbor.getMeasuredWidth(),
                    input.getPaddingBottom()
            );
            neighbor.setPadding(0, 0, 0, input.getPaddingBottom());
        }

        if (suffix != null) {
            input.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    for (Object what: s.getSpans(s.length() - 1, s.length(), ReplacementSpan.class)) {
                        s.removeSpan(what);
                    }

                    if (s.length() > 0) {
                        s.setSpan(new ReplacementSpan() {

                            @Override
                            public int getSize(@NonNull Paint paint, CharSequence text, int start, int end, @Nullable Paint.FontMetricsInt fm) {
                                return (int) paint.measureText(text, start, end);
                            }

                            @Override
                            public void draw(@NonNull Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, @NonNull Paint paint) {
                                canvas.drawText(text.subSequence(start, end).toString() + (end == text.length() ? "%" : ""), x, y, paint);
                            }

                        }, s.length() - 1, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                }

            });
        }
    }

    private void styleMultiChoice(TextView input, String[] choices, int defaultChoice, Runnable extraWork) {
        int enabledColor = ContextCompat.getColor(getContext(), R.color.ht_theme);
        int disabledColor = Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2);
        input.setTextColor(getColorStateList(enabledColor, disabledColor));
        input.setTypeface(input.getTypeface(), Typeface.BOLD);
        input.setText(choices[defaultChoice]);
        MultiChoicePopup subscriptionPeriodPopup = new MultiChoicePopup(input, choices, (index, choice) -> {
            input.setText(choice);
            if (extraWork != null) {
                extraWork.run();
            }
        });
        input.setOnClickListener(v -> subscriptionPeriodPopup.show());
    }

    private void updateBundleViews() {
        boolean enabled = mCheckBundle.isChecked();

        mBundleSessionCount.setEnabled(enabled);
        mBundleDiscountPercent.setEnabled(enabled);
    }

    private void updateSubscriptionViews() {
        boolean enabled = mCheckSubscription.isChecked();

        mSubscriptionPeriod.setEnabled(enabled);
        mSubscriptionPrice.setEnabled(enabled);
    }

    private void updateBundleCalculatedPrice() {
        int totalPrice = calculateBundlePrice();

        if (totalPrice >= 0) {
            String currency = mCurrency.getText().toString();

            mBundleCalculatedPrice.setText(totalPrice + " " + currency);
        }
        else {
            mBundleCalculatedPrice.setText("");
        }
    }

    private void updateSubscriptionPer() {
        mSubscriptionPer.setText(mCurrency.getText().toString() + " " + mSubscriptionPeriod.getText().toString());
    }

    private int calculateBundlePrice() {
        try {
            int itemRawPrice = Integer.parseInt(mFixedPrice.getText().toString());
            int count = Integer.parseInt(mBundleSessionCount.getText().toString());
            int discountPercent = Integer.parseInt(mBundleDiscountPercent.getText().toString());

            return itemRawPrice * count * (100 - discountPercent) / 100;
        } catch (Throwable t) {
            return -1;
        }
    }

    public PricingInfo getPricingInfo() {
        try {
            int price = Integer.parseInt(mFixedPrice.getText().toString());
            String currency = mCurrency.getText().toString();
            String rateType = mRateType.getText().toString();
            int bundleCount = mCheckBundle.isChecked() ? Integer.parseInt(mBundleSessionCount.getText().toString()) : 0;
            int bundleDiscountPercent = bundleCount == 0 ? 0 : Integer.parseInt(mBundleDiscountPercent.getText().toString());
            String subscriptionPeriod = mCheckSubscription.isChecked() ? mSubscriptionPeriod.getText().toString() : null;
            int subscriptionPrice = subscriptionPeriod == null ? 0 : Integer.parseInt(mSubscriptionPrice.getText().toString());

            return new PricingInfo(price, currency, rateType, bundleCount, bundleDiscountPercent, subscriptionPeriod, subscriptionPrice);
        } catch (Throwable t) {
            return null;
        }
    }

    public void setPricingInfo(PricingInfo pricingInfo) {
        mFixedPrice.setText(pricingInfo.price == 0 ? "" : String.valueOf(pricingInfo.price));
        mCurrency.setText(pricingInfo.currency == null ? CURRENCIES[1] : pricingInfo.currency);
        mRateType.setText(pricingInfo.rateType == null ? RATE_TYPES[0] : pricingInfo.rateType);

        if (pricingInfo.bundleCount > 0) {
            mCheckBundle.setChecked(true);
            mBundleSessionCount.setText(String.valueOf(pricingInfo.bundleCount));
            mBundleDiscountPercent.setText(pricingInfo.bundleDiscountPercent == 0 ? "" : String.valueOf(pricingInfo.bundleDiscountPercent));
        }
        else {
            mCheckBundle.setChecked(false);
            mBundleSessionCount.setText("");
            mBundleDiscountPercent.setText("");
        }
        updateBundleViews();

        if (pricingInfo.subscriptionPeriod != null) {
            mCheckSubscription.setChecked(true);
            mSubscriptionPeriod.setText(pricingInfo.subscriptionPeriod);
            mSubscriptionPrice.setText(pricingInfo.subscriptionPrice == 0 ? "" : String.valueOf(pricingInfo.subscriptionPrice));
        }
        updateSubscriptionViews();
    }

    private static ColorStateList getColorStateList(int enabledColor, int disabledColor) {
        int[] enabled = new int[] { android.R.attr.state_enabled };
        int[] disabled = new int[0];

        return new ColorStateList(new int[][] {enabled, disabled}, new int[] {enabledColor, disabledColor});
    }

    private static class BaseTextWatcher implements TextWatcher {

        private Runnable mTask;

        private BaseTextWatcher(Runnable task) {
            mTask = task;
        }

        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

        @Override
        public void afterTextChanged(Editable s) {
            mTask.run();
        }

    }

}
