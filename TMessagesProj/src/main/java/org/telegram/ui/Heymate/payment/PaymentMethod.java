package org.telegram.ui.Heymate.payment;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import works.heymate.beta.R;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.wallet.Wallet;
import works.heymate.ramp.Ramp;

public abstract class PaymentMethod {

    public static final PaymentMethod BANK_TRANSFER;
    public static final PaymentMethod CREDIT_CARD;
    public static final PaymentMethod PAYPAL;
    public static final PaymentMethod APPLE_PAY;
    public static final PaymentMethod ALFAJORES;

    static {
        BANK_TRANSFER = new PaymentMethod(R.drawable.hm_ic_bank_transfer, "Bank Transfer", createFeeMap(0), Currency.EUR, Currency.USD) {

            @Override
            public boolean execute(Context context, Money amount) {
                context.startActivity(BankTransferInformationActivity.getIntent(context, amount));
                return true;
            }

        };

        CREDIT_CARD = new PaymentMethod(R.drawable.hm_ic_credit_card, "Credit Card", createFeeMap(0), Currency.EUR, Currency.USD) {

            @Override
            public boolean execute(Context context, Money amount) {
                String topUpAmount = CurrencyUtil.centsToBlockChainValue(amount.getCents()).toString();

                String phoneNumber = TG2HM.getCurrentPhoneNumber();
                Wallet wallet = Wallet.get(context, phoneNumber);

                Ramp.getDialog(context, wallet.getAddress(), topUpAmount, () -> PaymentController.get(context).resumePayment()).show();

                return true;
            }

        };

        PAYPAL = new PaymentMethod(R.drawable.hm_ic_paypal, "Paypal", createFeeMap(400), Currency.EUR, Currency.USD) {

            @Override
            public boolean execute(Context context, Money amount) {
                return false;
            }

        };

        APPLE_PAY = new PaymentMethod(R.drawable.hm_ic_apple_pay, "Apple Pay", createFeeMap(400), Currency.EUR, Currency.USD) {

            @Override
            public boolean execute(Context context, Money amount) {
                return false;
            }

        };

        ALFAJORES = new PaymentMethod(R.drawable.menu_wallet, "Alfajores Testnet", createFeeMap(0), Currency.EUR, Currency.USD) {

            @Override
            public boolean execute(Context context, Money amount) {
                String phoneNumber = TG2HM.getCurrentPhoneNumber();
                Wallet wallet = Wallet.get(context, phoneNumber);

                TextView addressView = new TextView(context);
                addressView.setText(wallet.getAddress() + "\nhttps://celo.org/developers/faucet");
                addressView.setAutoLinkMask(Linkify.WEB_URLS);
                addressView.setTextIsSelectable(true);
                addressView.setMovementMethod(LinkMovementMethod.getInstance());

                new AlertDialog.Builder(context)
                        .setTitle("Initiate payment?")
                        .setView(addressView)
                        .setPositiveButton("Go", (dialog, which) -> {
                            dialog.dismiss();
                            PaymentController.get(ApplicationLoader.applicationContext).resumePayment();
                        })
                        .show();

                return true;
            }

        };
    }

    private static final PaymentMethod[] DEMO_MAIN = { BANK_TRANSFER, CREDIT_CARD, PAYPAL, APPLE_PAY };
    private static final PaymentMethod[] DEMO_ALFAJORES = { BANK_TRANSFER, CREDIT_CARD, PAYPAL, APPLE_PAY, ALFAJORES };
    private static final PaymentMethod[] REAL_MAIN = { BANK_TRANSFER, CREDIT_CARD };
    private static final PaymentMethod[] REAL_ALFAJORES = { BANK_TRANSFER, CREDIT_CARD, ALFAJORES };

    public static final PaymentMethod[] PAYMENT_METHODS =
            HeymateConfig.DEMO ? (HeymateConfig.MAIN_NET ? DEMO_MAIN : DEMO_ALFAJORES) :
                    (HeymateConfig.MAIN_NET ? REAL_MAIN : REAL_ALFAJORES);

    private final int mIconResId;
    private final String mTitle;
    private final Map<Currency, Money> mFee;

    public final List<Currency> supportedCurrencies;

    PaymentMethod(int iconResId, String title, Map<Currency, Money> fee, Currency... supportedCurrencies) {
        mIconResId = iconResId;
        mTitle = title;
        mFee = fee;

        this.supportedCurrencies = Arrays.asList(supportedCurrencies);
    }

    public View createView(Context context, Currency currency) {
        FrameLayout view = new FrameLayout(context);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(56)));

        AppCompatImageView image = new AppCompatImageView(context);
        Drawable icon = AppCompatResources.getDrawable(context, mIconResId).mutate();
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton), PorterDuff.Mode.SRC_IN);
        image.setImageDrawable(icon);
        view.addView(image, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 16, 0, 0, 0));

        TextView title = new TextView(context);
        title.setTextSize(16);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setText(mTitle); // TODO TEXTS
        view.addView(title, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 64, 0, 0, 0));

        TextView fee = new TextView(context);
        fee.setTextSize(12);
        fee.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        fee.setText("Fee: " + mFee.get(currency).toString());
        view.addView(fee, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 16, 0));

        View divider = new View(context);
        divider.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        view.addView(divider, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 1, Gravity.BOTTOM, 16, 0, 0, 0));

        return view;
    }

    abstract public boolean execute(Context context, Money amount);

    private static Map<Currency, Money> createFeeMap(long amount) {
        Map<Currency, Money> map = new HashMap<>();

        map.put(Currency.USD, Money.create(amount, Currency.USD));
        map.put(Currency.EUR, Money.create(amount, Currency.EUR));

        return map;
    }

}
