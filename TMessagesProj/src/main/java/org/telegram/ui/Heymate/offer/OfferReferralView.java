package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.TG2HM;

import works.heymate.beta.R;
import works.heymate.core.Money;

public class OfferReferralView extends SequenceLayout {

    private TextView mAmount;

    public OfferReferralView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferReferralView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferReferralView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.offer_referral, this, true);
        addSequences(R.xml.sequences_offer_referral);

        TextView title = findViewById(R.id.title);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        title.setText("Referrral Sharing"); // TODO Texts

        ImageView image = findViewById(R.id.image);
        image.setImageDrawable(TG2HM.getThemedDrawable(R.drawable.hm_ic_gift, Theme.getColor(Theme.key_windowBackgroundWhiteBlueText)));

        TextView header = findViewById(R.id.header);
        header.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        header.setText("Referral budget for this offer is");

        mAmount = findViewById(R.id.amount);
        mAmount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        TextView info = findViewById(R.id.info);
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        info.setText("Sounds great? Refer your friends and you’ll get rewarded. Just forward this offer to your friends, then when they buy the offer you get this gift.");
    }

    public void setReferralAmount(int amount) {
        mAmount.setText(amount + "%");
    }

}
