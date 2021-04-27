package org.telegram.ui.Heymate;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.telegram.messenger.LocaleController;
import works.heymate.beta.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.RadioButtonCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Components.LayoutHelper;

import works.heymate.core.Texts;
import works.heymate.core.wallet.Security;
import works.heymate.core.wallet.Wallet;

public class SecureWalletActivity extends BaseFragment {

    private Runnable mFinishTask = null;

    private RadioButtonCellWithIcon mButtonBiometric;
    private RadioButtonCellWithIcon mButtonPin;

    private Wallet mWallet;

    public SecureWalletActivity() {

    }

    public SecureWalletActivity(Runnable finishTask) {
        mFinishTask = finishTask;
    }

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context) {
        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        FrameLayout container = new FrameLayout(context);
        container.addView(content, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        container.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        mButtonBiometric = new RadioButtonCellWithIcon(context);
        mButtonBiometric.setTextAndValue(Texts.get(Texts.SECURE_BIOMETRIC).toString(), Texts.get(Texts.SECURE_BIOMETRIC_DESCRIPTION).toString(), false, false);
        mButtonBiometric.setIcon(works.heymate.beta.R.drawable.ic_biometric);
        content.addView(mButtonBiometric, LayoutHelper.createLinear(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0, 22, 0, 0));

        mButtonPin = new RadioButtonCellWithIcon(context);
        mButtonPin.setTextAndValue(Texts.get(Texts.SECURE_PIN).toString(), Texts.get(Texts.SECURE_PIN_DESCRIPTION).toString(), false, false);
        mButtonPin.setIcon(works.heymate.beta.R.drawable.ic_pin);
        content.addView(mButtonPin, LayoutHelper.createLinear(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0, 0, 0, 0));

        content.addView(new ShadowSectionCell(context, 12, Theme.getColor(Theme.key_windowBackgroundGray)), LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));

        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.SECURE));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();

                    if (mFinishTask != null) {
                        mFinishTask.run();
                    }
                }
            }
        });

        mButtonBiometric.setOnClickListener(v -> setSecurityMode(Security.BIOMETRIC));
        mButtonPin.setOnClickListener(v -> setSecurityMode(Security.PIN));

        mWallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        updateState();

        return container;
    }

    private void setSecurityMode(int mode) {
        if (mode == Security.INSECURE) {
            Security.setSecurityMode(mWallet, mode);
            updateState();
            return;
        }

        if (Security.isSecurityModeAvailable(mode, getParentActivity())) {
            Security.setSecurityMode(mWallet, mode);
            updateState();
        }
        else {
            startActivityForResult(Security.getEnableIntent(mode), mode);
        }
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (Security.isSecurityModeAvailable(requestCode, getParentActivity())) {
            setSecurityMode(requestCode);
        }
    }

    private void updateState() {
        int securityMode = Security.getSecurityMode(mWallet);

        int supportedSecurityModes = Security.getSupportedSecurityModes(getParentActivity());

        if ((securityMode & supportedSecurityModes) != securityMode) {
            setSecurityMode(Security.INSECURE);
            return;
        }

        if (securityMode == Security.BIOMETRIC) {
            mButtonBiometric.setChecked(true, true);
            mButtonPin.setChecked(false, true);
        }
        else if (securityMode == Security.PIN) {
            mButtonBiometric.setChecked(false, true);
            mButtonPin.setChecked(true, true);
        }

        boolean biometricSupported = (supportedSecurityModes & Security.BIOMETRIC) == Security.BIOMETRIC;
        boolean pinSupported = (supportedSecurityModes & Security.PIN) == Security.PIN;

        mButtonBiometric.setEnabled(biometricSupported);
        mButtonBiometric.setAlpha(biometricSupported ? 1f : 0.6f);
        mButtonPin.setEnabled(pinSupported);
        mButtonPin.setAlpha(pinSupported ? 1f : 0.6f);
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);

        if (mFinishTask != null) {
            mFinishTask.run();
        }

        return super.onBackPressed();
    }

    private static class RadioButtonCellWithIcon extends RadioButtonCell {

        private ImageView mImageIcon;

        public RadioButtonCellWithIcon(Context context) {
            super(context);

            mImageIcon = new ImageView(context);
            mImageIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageIcon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText2), PorterDuff.Mode.SRC_IN);
            addView(mImageIcon, LayoutHelper.createFrame(24, 24, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, (LocaleController.isRTL ? 23 : 61), 9, (LocaleController.isRTL ? 61 : 23), 0));
        }

        public void setIcon(int resId) {
            mImageIcon.setImageResource(resId);
        }

    }

}
