package org.telegram.ui.Heymate.createoffer;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ReplacementSpan;
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

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.HeymateConfig;

import works.heymate.core.Currency;
import works.heymate.core.Money;
import org.telegram.ui.Heymate.widget.MultiChoicePopup;

import works.heymate.beta.R;
import works.heymate.model.Pricing;

public class PriceInputItem extends ExpandableItem {

    private static final Currency[] DEMO_CURRENCIES = { Currency.USD, Currency.EUR };
    private static final Currency[] REAL_CURRENCIES = { Currency.EUR };

    private static final int DEFAULT_CURRENCY_CHOICE = HeymateConfig.DEMO ? 1 : 0;

    private static final String[] DEMO_RATE_TYPES = { "Per Session", "Per Hour" };
    private static final String[] REAL_RATE_TYPES = { "Per Session" };

    private static final Currency[] CURRENCIES = HeymateConfig.DEMO ? DEMO_CURRENCIES : REAL_CURRENCIES;
    private static final String[] RATE_TYPES = HeymateConfig.DEMO ? DEMO_RATE_TYPES : REAL_RATE_TYPES;
    public static final String[] SUBSCRIPTION_PERIODS = { "Per month", "Per year" };

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

        String[] currencies = new String[CURRENCIES.length];
        for (int i = 0; i < CURRENCIES.length; i++) {
            currencies[i] = CURRENCIES[i].name();
        }

        styleMultiChoice(mCurrency, currencies, DEFAULT_CURRENCY_CHOICE, () -> {
            updateBundleCalculatedPrice();
            updateSubscriptionPer();
        });
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
            Currency currency = Currency.forName(mCurrency.getText().toString());

            mBundleCalculatedPrice.setText(Money.create(totalPrice * 100, currency).toString());
        }
        else {
            mBundleCalculatedPrice.setText("");
        }
    }

    private void updateSubscriptionPer() {
        Currency currency = Currency.forName(mCurrency.getText().toString());
        mSubscriptionPer.setText(currency.symbol() + " " + mSubscriptionPeriod.getText().toString());
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

    public Pricing getPricing() {
        try {
            int price = Integer.parseInt(mFixedPrice.getText().toString());
            Currency currency = Currency.forName(mCurrency.getText().toString());
            String rateType = mRateType.getText().toString();
            int bundleCount = mCheckBundle.isChecked() ? Integer.parseInt(mBundleSessionCount.getText().toString()) : 0;
            int bundleDiscountPercent = bundleCount == 0 ? 0 : Integer.parseInt(mBundleDiscountPercent.getText().toString());
            String subscriptionPeriod = mCheckSubscription.isChecked() ? mSubscriptionPeriod.getText().toString() : null;
            int subscriptionPrice = subscriptionPeriod == null ? 0 : Integer.parseInt(mSubscriptionPrice.getText().toString());

            return new Pricing(price, currency, rateType, bundleCount, bundleDiscountPercent, subscriptionPeriod, subscriptionPrice);
        } catch (Throwable t) {
            return null;
        }
    }

    public void setPricing(Pricing pricing) {
        mFixedPrice.setText(pricing.getPrice() == 0 ? "" : String.valueOf(pricing.getPrice()));
        mCurrency.setText(pricing.getCurrency() == null ? CURRENCIES[DEFAULT_CURRENCY_CHOICE].name() : pricing.getCurrency());

        mRateType.setText(RATE_TYPES[0]);

        for (String rateType: RATE_TYPES) {
            if (rateType.equals(pricing.getRateType())) {
                mRateType.setText(rateType);
                break;
            }
        }

        if (pricing.getBundleCount() > 0) {
            mCheckBundle.setChecked(true);
            mBundleSessionCount.setText(String.valueOf(pricing.getBundleCount()));
            mBundleDiscountPercent.setText(pricing.getBundleDiscountPercent() == 0 ? "" : String.valueOf(pricing.getBundleDiscountPercent()));
        }
        else {
            mCheckBundle.setChecked(false);
            mBundleSessionCount.setText("");
            mBundleDiscountPercent.setText("");
        }
        updateBundleViews();

        if (pricing.getSubscriptionPeriod() != null) {
            mCheckSubscription.setChecked(true);
            mSubscriptionPeriod.setText(pricing.getSubscriptionPeriod());
            mSubscriptionPrice.setText(pricing.getSubscriptionPrice() == 0 ? "" : String.valueOf(pricing.getSubscriptionPrice()));
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
