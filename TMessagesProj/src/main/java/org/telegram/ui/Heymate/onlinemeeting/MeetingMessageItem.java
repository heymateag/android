package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.amplifyframework.datastore.generated.model.Offer;
import com.amplifyframework.datastore.generated.model.Reservation;
import com.yashoid.sequencelayout.SequenceLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

import works.heymate.beta.R;

public class MeetingMessageItem extends SequenceLayout {

    private ImageView mImageUser;
    private TextView mTitle;
    private TextView mSubCategory;
    private TextView mDescription;
    private TextView mTimer;
    private TextView mMoreDetails;
    private TextView mJoin;

    private Offer mOffer = null;
    private Reservation mReservation = null;

    public MeetingMessageItem(Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public MeetingMessageItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public MeetingMessageItem(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.item_meetingmessage, this, true);
        addSequences(R.xml.sequences_item_meetingmessage);

        View background = findViewById(R.id.background);
        mImageUser = findViewById(R.id.image_user);
        mTitle = findViewById(R.id.title);
        mSubCategory = findViewById(R.id.subcategory);
        mDescription = findViewById(R.id.description);
        mTimer = findViewById(R.id.timer);
        mMoreDetails = findViewById(R.id.more_details);
        mJoin = findViewById(R.id.join);

        int cornerRadius = AndroidUtilities.dp(8);

        background.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));

        mTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mSubCategory.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mTimer.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));

        mMoreDetails.setBackground(Theme.createRoundRectDrawable(cornerRadius, Theme.getColor(Theme.key_windowBackgroundWhite)));
        mMoreDetails.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mMoreDetails.setTypeface(mMoreDetails.getTypeface(), Typeface.BOLD);
        mMoreDetails.setText("More Details"); // TODO Texts

        mJoin.setBackground(Theme.createRoundRectDrawable(cornerRadius, ContextCompat.getColor(context, R.color.ht_theme)));
        mJoin.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mJoin.setTypeface(mJoin.getTypeface(), Typeface.BOLD);
        mJoin.setText("Join Session");

        // TODO The rest
    }

    public void setOffer(Offer offer) {
        // TODO
    }

    public void setReservation(Reservation reservation) {
        // TODO
    }

}
