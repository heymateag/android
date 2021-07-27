package org.telegram.ui.Heymate.offer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.ui.ActionBar.Theme;

import works.heymate.beta.R;

public class OfferTermsView extends SequenceLayout {

    private TextView mTerms;

    public OfferTermsView(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public OfferTermsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public OfferTermsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.offer_terms, this, true);
        addSequences(R.xml.sequences_offer_terms);

        TextView title = findViewById(R.id.title);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        title.setText("Terms and conditions"); // TODO Texts

        mTerms = findViewById(R.id.terms);
        mTerms.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        findViewById(R.id.divider).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        TextView seeMore = findViewById(R.id.see_more);
        seeMore.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
        seeMore.setText("See more");
        seeMore.setOnClickListener(v -> {
            // TODO
        });

        TextView info = findViewById(R.id.info);
        info.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        info.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        info.setText("By purchasing this offer and continuing the process, you are bound to the above Terms and you indicate your continued acceptance of these Terms and conditions.");
    }

    public void setTerms(String terms) {
        mTerms.setText(terms);
    }

}
