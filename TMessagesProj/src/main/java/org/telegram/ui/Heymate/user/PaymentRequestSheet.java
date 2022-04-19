package org.telegram.ui.Heymate.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.SendMessagesHelper;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.TG2HM;

import works.heymate.beta.R;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.model.Users;

public class PaymentRequestSheet extends BottomSheet {

    private static final String TAG = "PaymentRequestSheet";

    private SequenceLayout mLayout;
    private TextView mTitleSend;
    private TextView mCurrencySend;
    private View mInputSendBackground;
    private EditText mInputSend;
    private EditText mDescription;
    private TextView mButton;

    private Currency mFromCurrency;
    private Money mSendAmount;

    public PaymentRequestSheet(Context context, long dialogId) {
        super(context, true);

        setDimBehindAlpha(0x4D);
        setDimBehind(true);
        setCanDismissWithSwipe(true);
        setCancelable(true);

        setTitle("Send Payment Request", true);

        mLayout = (SequenceLayout) LayoutInflater.from(context).inflate(R.layout.hm_sheet_paymentrequest, null, false);

        mTitleSend = mLayout.findViewById(R.id.title_send);
        mCurrencySend = mLayout.findViewById(R.id.input_send_currency);
        mInputSendBackground = mLayout.findViewById(R.id.input_send_background);
        mInputSend = mLayout.findViewById(R.id.input_send);
        mDescription = mLayout.findViewById(R.id.description);
        mButton = mLayout.findViewById(R.id.send);

        setupText(mTitleSend, "Request amount");
        setupInput(mInputSend, "00.00");
        setupInput(mDescription, "Message (optional)");

        Drawable box = Theme.createRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_windowBackgroundGray));
        mInputSendBackground.setBackground(box);
        mDescription.setBackground(box);

        setCustomView(mLayout);

        mFromCurrency = TG2HM.getDefaultCurrency();

        mSendAmount = Money.create(0, mFromCurrency);

        mCurrencySend.setText(mFromCurrency.symbol());

        mCurrencySend.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Change currency")
                    .setMessage("Select the currency for your request.")
                    .setItems(Currency.CURRENCY_NAMES, (dialog, which) -> {
                        mFromCurrency = Currency.forName(Currency.CURRENCY_NAMES[which]);
                        mCurrencySend.setText(mFromCurrency.symbol());
                        mSendAmount = Money.create(mSendAmount.getCents(), mFromCurrency);
                    })
                    .show();
        });

        mInputSend.addTextChangedListener(new TextWatcher() {

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString();

                if (text.length() == 0) {
                    text = "0";
                }

                long cents;

                try {
                    cents = Math.round(Double.parseDouble(text) * 100);
                } catch (Throwable t) {
                    cents = 0;
                }

                mSendAmount.setCents(cents);

                updateButton("Send", cents > 0);
            }

        });

        updateButton("Send", false);

        mButton.setOnClickListener(v -> {
            String message = PaymentRequestUtils.serialize(Users.currentUser, mSendAmount, TG2HM.getWallet().getAddress(), mDescription.getText().toString());

            SendMessagesHelper.getInstance(getCurrentAccount()).sendMessage(message, dialogId, null, null, null, false, null, null, null, true, 0, null);

            dismiss();
        });
    }

    private void updateButton(String text, boolean enabled) {
        mButton.setText(text); // TODO texts
        mButton.setEnabled(enabled);
        mButton.setTextColor(Theme.getColor(Theme.key_dialogFloatingIcon));
        mButton.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(enabled ? Theme.key_dialogFloatingButton : Theme.key_windowBackgroundWhiteGrayIcon)));
    }

    private void setupText(TextView textView, String textKey) {
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setText(textKey); // TODO texts
    }

    private void setupInput(EditText input, String hint) {
        input.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        input.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        input.setHint(hint); // TODO texts
    }

}
