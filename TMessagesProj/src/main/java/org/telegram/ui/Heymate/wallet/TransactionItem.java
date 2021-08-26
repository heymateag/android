package org.telegram.ui.Heymate.wallet;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.TG2HM;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;

import works.heymate.beta.R;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.wallet.Wallet;

public class TransactionItem extends SequenceLayout {

    private TextView mTitle;
    private TextView mDescription;
    private TextView mAmount;
    private TextView mTime;

    private String mUSDAddress;
    private String mEURAddress;

    private String mAddress;

    public TransactionItem(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public TransactionItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public TransactionItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.item_transaction, this, true);
        addSequences(R.xml.sequences_item_transaction);

        mTitle = findViewById(R.id.title);
        mDescription = findViewById(R.id.description);
        mAmount = findViewById(R.id.amount);
        mTime = findViewById(R.id.time);
        View divider = findViewById(R.id.divider);

        mTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mTime.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2));
        divider.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        mAddress = Wallet.get(context, TG2HM.getCurrentPhoneNumber()).getAddress();
    }

    public void setCurrencyAddresses(String usdAddress, String eurAddress) {
        mUSDAddress = usdAddress;
        mEURAddress = eurAddress;
    }

    public void setTransaction(JSONObject transaction) {
        try {
            String from = transaction.getString("from");
            String to = transaction.getString("to");
            long timestamp = Long.parseLong(transaction.getString("timeStamp")) * 1000L;
            BigInteger value = new BigInteger(transaction.getString("value"));
            String contract = transaction.getString("contractAddress");

            Currency currency = mEURAddress.equals(contract) ? Currency.EUR : Currency.USD;

            boolean received = mAddress.equals(to);

            long rawAmount = CurrencyUtil.blockChainValueToCents(value);
            Money amount = Money.create(rawAmount, currency);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM, dd HH:mm");

            mTitle.setText(received ? "Received payment" : "Purchase transaction");
            mDescription.setText("Details not available");
            mTime.setText(simpleDateFormat.format(new Date(timestamp)));

            if (received) {
                mAmount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton));
                mAmount.setText("+" + amount);
            }
            else {
                mAmount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
                mAmount.setText("-" + amount);
            }
        } catch (JSONException e) {

        }
    }

}
