package org.telegram.ui.Heymate.createoffer;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.yashoid.sequencelayout.SequenceLayout;
import com.yashoid.sequencelayout.Span;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RadioButton;

import works.heymate.beta.R;

public class PromotionDialog extends Dialog {

    private static final long EXPAND_DURATION = 300;

    public interface OnPromotionDecisionCallback {

        void onPromote(int percentage);

        void onShare();

    }

    private OnPromotionDecisionCallback mCallback;

    private RadioButton mRadioReferral;
    private TextView mReferralTitle;
    private TextView mReferralDescription;
    private TextView mReferralMoreInfo;
    private TextView mReferralInfoToggle;
    private TextView mPercentageInfo;
    private EditText mEditPercentage;
    private TextView mTextPercent;
    private RadioButton mRadioShare;
    private TextView mShareTitle;
    private TextView mButtonCancel;
    private TextView mButtonPromote;

    private Drawable mExpandDrawable;
    private Drawable mCollapseDrawable;

    private boolean mCollapsed = true;
    private Span mMoreInfoSpan;
    private ValueAnimator mExpandAnimator = null;

    public PromotionDialog(@NonNull Context context, OnPromotionDecisionCallback callback) {
        super(context);
        setCancelable(true);

        mCallback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawable(new ColorDrawable(Theme.getColor(Theme.key_dialogBackground)));
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        SequenceLayout root = (SequenceLayout) getLayoutInflater().inflate(R.layout.dialog_promotion, null, false);
        setContentView(root, new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mMoreInfoSpan = root.findSequenceById("spine").getSpans().get(5);

        mRadioReferral = findViewById(R.id.radio_referral);
        mReferralTitle = findViewById(R.id.title_referral);
        mReferralDescription = findViewById(R.id.description_referral);
        mReferralMoreInfo = findViewById(R.id.moreinfo_referral);
        mReferralInfoToggle = findViewById(R.id.infotoggle_referral);
        mPercentageInfo = findViewById(R.id.text_percentageinfo);
        mEditPercentage = findViewById(R.id.edit_percentage);
        mTextPercent = findViewById(R.id.text_percent);
        mRadioShare = findViewById(R.id.radio_share);
        mShareTitle = findViewById(R.id.title_share);
        mButtonCancel = findViewById(R.id.button_cancel);
        mButtonPromote = findViewById(R.id.button_promote);

        int themeColor = ContextCompat.getColor(getContext(), R.color.ht_theme);

        mRadioReferral.setSize(AndroidUtilities.dp(20));
        mRadioReferral.setColor(Theme.getColor(Theme.key_radioBackground), themeColor);
        mRadioShare.setSize(AndroidUtilities.dp(20));
        mRadioShare.setColor(Theme.getColor(Theme.key_radioBackground), themeColor);

        mReferralTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mReferralTitle.setTextSize(18);
        mReferralTitle.setTypeface(mReferralTitle.getTypeface(), Typeface.BOLD);
        mReferralTitle.setText("Referral Sharing"); // TODO Move string constants to Texts

        mShareTitle.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mShareTitle.setTextSize(18);
        mShareTitle.setTypeface(mShareTitle.getTypeface(), Typeface.BOLD);
        mShareTitle.setText("Simple Share");

        mReferralDescription.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mReferralDescription.setTextSize(14);
        mReferralDescription.setText("Improve the chances of buying by 80% more than simple sharing.");

        mReferralMoreInfo.setTextColor(Theme.getColor(Theme.key_dialogTextGray));
        mReferralMoreInfo.setTextSize(14);
        mReferralMoreInfo.setText("Other users will be motivated to promote your offer and win the referral prize. Multiple people can form a referral chain.");

        mExpandDrawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_arrow_drop_down).mutate();
        mExpandDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);
        mCollapseDrawable = ContextCompat.getDrawable(getContext(), R.drawable.collapse_up).mutate();
        mCollapseDrawable.setColorFilter(themeColor, PorterDuff.Mode.SRC_IN);

        mReferralInfoToggle.setTextColor(themeColor);
        mReferralInfoToggle.setTextSize(12);
        mReferralInfoToggle.setCompoundDrawablePadding(AndroidUtilities.dp(4));
        mReferralInfoToggle.setGravity(Gravity.CENTER_VERTICAL);
        mReferralInfoToggle.setText("more info");
        mReferralInfoToggle.setCompoundDrawablesWithIntrinsicBounds(mExpandDrawable, null, null, null);

        mPercentageInfo.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mPercentageInfo.setTextSize(14);
        mPercentageInfo.setText("Define % you pay in total when someone buys.");

        mEditPercentage.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mEditPercentage.setHintTextColor(Theme.getColor(Theme.key_dialogTextHint));
        mEditPercentage.setTextSize(13);
        mEditPercentage.setHint("00");

        mTextPercent.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        mTextPercent.setTextSize(13);
        mTextPercent.setText("%");

        mButtonCancel.setTextColor(Theme.getColor(Theme.key_dialogTextGray2));
        mButtonCancel.setTextSize(14);
        mButtonCancel.setText("Cancel");

        mButtonPromote.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        mButtonPromote.setTextSize(14);
        mButtonPromote.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), themeColor));
        mButtonPromote.setText("Promote");

        mReferralInfoToggle.setOnClickListener(v -> {
            if (mExpandAnimator != null) {
                mExpandAnimator.cancel();
                mExpandAnimator = null;
            }

            mCollapsed = !mCollapsed;

            mReferralMoreInfo.measure(
                    View.MeasureSpec.makeMeasureSpec(mReferralDescription.getWidth(), View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );
            int moreInfoHeight = mReferralMoreInfo.getMeasuredHeight();

            int startValue = (int) mMoreInfoSpan.size;
            int endValue = mCollapsed ? 0 : moreInfoHeight;

            mExpandAnimator = new ValueAnimator();
            mExpandAnimator.setDuration(EXPAND_DURATION);
            mExpandAnimator.setIntValues(startValue, endValue);
            mExpandAnimator.addUpdateListener(animation -> {
                int height = (int) animation.getAnimatedValue();
                if (mMoreInfoSpan.size == height) {
                    return;
                }
                mMoreInfoSpan.size = height;
                mReferralMoreInfo.requestLayout();
            });
            mExpandAnimator.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mMoreInfoSpan.size = endValue;
                    mReferralMoreInfo.requestLayout();

                    if (mCollapsed) {
                        mReferralInfoToggle.setText("more info");
                        mReferralInfoToggle.setCompoundDrawablesWithIntrinsicBounds(mExpandDrawable, null, null, null);
                    }
                    else {
                        mReferralInfoToggle.setText("close");
                        mReferralInfoToggle.setCompoundDrawablesWithIntrinsicBounds(mCollapseDrawable, null, null, null);
                    }
                }

                @Override public void onAnimationStart(Animator animation) { }
                @Override public void onAnimationCancel(Animator animation) { }
                @Override public void onAnimationRepeat(Animator animation) { }

            });
            mExpandAnimator.start();
        });

        mRadioReferral.setOnClickListener(this::toggleReferral);
        mReferralTitle.setOnClickListener(this::toggleReferral);
        mRadioShare.setOnClickListener(this::toggleShare);
        mShareTitle.setOnClickListener(this::toggleShare);

        mButtonCancel.setOnClickListener(v -> dismiss());
        mButtonPromote.setOnClickListener(v -> {
            if (mRadioShare.isChecked()) {
                dismiss();
                mCallback.onShare();
                return;
            }

            String percentageText = mEditPercentage.getText().toString();

            if (percentageText.length() == 0) {
                Toast.makeText(getContext(), "Referral percentage is not set.", Toast.LENGTH_SHORT).show();
                return;
            }

            int percentage = Integer.parseInt(percentageText);

            dismiss();
            mCallback.onPromote(percentage);
        });

        mRadioShare.setChecked(true, false);
    }

    private void toggleReferral(View v) {
        mRadioReferral.setChecked(true, true);
        mRadioShare.setChecked(false, true);
    }

    private void toggleShare(View v) {
        mRadioReferral.setChecked(false, true);
        mRadioShare.setChecked(true, true);
    }

}
