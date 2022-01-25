package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.ui.ActionBar.Theme;

import works.heymate.api.APIObject;
import works.heymate.beta.R;
import works.heymate.model.Offer;

public class OfferPaymentTermsView extends SequenceLayout {

    private TextView mDelayTitle;
    private TextView mDelayValue;
    private TextView mCancelTitle;
    private TextView mCancelValue;
    private TextView mCancel2Title;
    private TextView mCancel2Value;
    private TextView mDepositTitle;
    private TextView mDepositValue;

    public OfferPaymentTermsView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferPaymentTermsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferPaymentTermsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.offer_paymentterms, this, true);
        addSequences(R.xml.sequences_offer_paymentterms);

        TextView title = findViewById(R.id.title);
        mDelayTitle = findViewById(R.id.title_delay);
        mDelayValue = findViewById(R.id.value_delay);
        mCancelTitle = findViewById(R.id.title_cancel);
        mCancelValue = findViewById(R.id.value_cancel);
        mCancel2Title = findViewById(R.id.title_cancel2);
        mCancel2Value = findViewById(R.id.value_cancel2);
        mDepositTitle = findViewById(R.id.title_deposit);
        mDepositValue = findViewById(R.id.value_deposit);

        findViewById(R.id.divider_delay).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        findViewById(R.id.divider_cancel).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        findViewById(R.id.divider_cancel2).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        title.setText("Payment terms"); // TODO Texts

        mDelayTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mCancelTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mCancel2Title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDepositTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        mDelayValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mCancelValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mCancel2Value.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mDepositValue.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
    }

    public void setPaymentTerms(APIObject terms) {
        APIObject cancel1 = terms.getArray(Offer.PaymentTerms.CANCELLATION).getObject(0);
        APIObject cancel2 = terms.getArray(Offer.PaymentTerms.CANCELLATION).getObject(1);

        int delayMinutes = terms.getInt(Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.DURATION);
        int delayPercent = terms.getInt(Offer.PaymentTerms.DELAY_IN_START + "." + Offer.PaymentTerms.DelayInStart.PENALTY);
        int cancelMinutes = cancel1.getInt(Offer.PaymentTerms.Cancellation.RANGE);
        int cancelPercent = cancel1.getInt(Offer.PaymentTerms.Cancellation.PENALTY);
        int cancel2Minutes = cancel2.getInt(Offer.PaymentTerms.Cancellation.RANGE);
        int cancel2Percent = cancel2.getInt(Offer.PaymentTerms.Cancellation.PENALTY);
        int deposit = terms.getInt(Offer.PaymentTerms.DEPOSIT);

        mDelayTitle.setText("Delays in start by > " + delayMinutes + " min");
        mDelayValue.setText(delayPercent + "%");

        mCancelTitle.setText("Cancellation in < " + (cancelMinutes / 60) + " hr of start");
        mCancelValue.setText(cancelPercent + "%");

        mCancel2Title.setText("Cancellation in " + (cancelMinutes / 60) + "-" + (cancel2Minutes / 60) + " hr of start");
        mCancel2Value.setText(cancel2Percent + "%");

        mDepositTitle.setText("Initial deposit");
        mDepositValue.setText(deposit + "%");
    }

}
