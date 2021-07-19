package org.telegram.ui.Heymate.payment;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.HeymateRouter;

import works.heymate.beta.R;
import works.heymate.ramp.HTLC;
import works.heymate.ramp.Peymate;

public class BankTransferResultActivity extends BaseFragment implements Peymate.PaymentCheckListener {

    public static final String HOST = "bankTransferResult";

    public static Intent getIntent(Context context) {
        return HeymateRouter.createIntent(context, HOST, null);
    }

    private ImageView mImage;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mButton;

    @Override
    public View createView(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setClipChildren(false);
        content.setGravity(Gravity.CENTER);

        mImage = new AppCompatImageView(context);
        content.addView(mImage, LayoutHelper.createLinear(64, 64, Gravity.CENTER_HORIZONTAL));

        mTitle = new TextView(context);
        mTitle.setTextSize(20);
        mTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTitle.setGravity(Gravity.CENTER);
        content.addView(mTitle, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 24, 24, 24, 0));

        mDescription = new TextView(context);
        mDescription.setTextSize(14);
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        content.addView(mDescription, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 16, 0, 0));

        mButton = new TextView(context);
        mButton.setTextSize(14);
        mButton.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton));
        mButton.setBackground(Theme.createBorderRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton)));
        mButton.setGravity(Gravity.CENTER);
        mButton.setOnClickListener(v -> {
            PaymentController.get(getParentActivity()).resumePayment();
            finishFragment();
        });
        content.addView(mButton, LayoutHelper.createLinear(116, 36, Gravity.CENTER_HORIZONTAL, 0, 24, 0, 0));

        setStatus(HTLC.STATUS_CLEARED);

        Peymate.get(getParentActivity()).addListener(this);

        fragmentView = content;

        return content;
    }

    @Override
    public boolean onPaymentCheckResult(boolean success, String status) {
        if (status != null) {
            setStatus(status);

            if (HTLC.STATUS_SETTLED.equals(status)) {
                Peymate.get(getParentActivity()).removeListener(this);
            }
        }

        return true;
    }

    private void setStatus(String status) {
        if (getParentActivity() == null) {
            return;
        }

        Drawable icon = AppCompatResources.getDrawable(getParentActivity(), R.drawable.hm_ic_check).mutate();

        switch (status) {
            case HTLC.STATUS_CLEARED:
                mImage.setVisibility(View.VISIBLE);
                mImage.setImageDrawable(AppCompatResources.getDrawable(getParentActivity(), R.drawable.loading_animation2));
                mTitle.setText("Your payment process takes about 3 minutes");
                mDescription.setVisibility(View.GONE);
                mButton.setVisibility(View.GONE);
                return;
            case HTLC.STATUS_EXPIRED:
                mImage.setVisibility(View.VISIBLE);
                icon.setColorFilter(Theme.getColor(Theme.key_wallet_redText), PorterDuff.Mode.SRC_IN);
                mImage.setImageDrawable(icon);

                mTitle.setText("Your payment has expired");
                mDescription.setVisibility(View.GONE);
                mButton.setVisibility(View.VISIBLE);
                mButton.setText("Resume Payment");
                return;
            case HTLC.STATUS_SETTLED:
                mImage.setVisibility(View.VISIBLE);
                icon.setColorFilter(Theme.getColor(Theme.key_wallet_greenText), PorterDuff.Mode.SRC_IN);
                mImage.setImageDrawable(icon);

                mTitle.setText("Payment Successful");
                mDescription.setVisibility(View.GONE);
                mButton.setVisibility(View.VISIBLE);
                mButton.setText("Resume Payment");
                return;
        }
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();

        mImage = null;
        mTitle = null;
        mDescription = null;
        mButton = null;

        super.clearViews();
    }

}
