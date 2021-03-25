package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.widget.PinIndexIndicator;
import org.telegram.ui.Heymate.widget.PinInput;

import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;
import works.heymate.core.wallet.VerifiedStatus;
import works.heymate.core.wallet.Wallet;

public class AttestationActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private static final int ATTESTATION_COUNT = 3;  // TODO Connect all the counts to Celo SDK.

    private ImageView mImageStep;
    private TextView mTextTitle;
    private TextView mTextDescription;
    private PinInput mInputCode;
    private PinIndexIndicator mIndicatorStep;
    private TextView mTextNext;
    private ImageView mImageNext;
    private View mButtonNext;

    private Wallet mWallet;

    private boolean mCanUpdateVerifiedStatus = true;
    private boolean mHasPendingAttestationRequest = false;
    private boolean mCanRequestMoreAttestations = true;

    private String mPin = null;

    private boolean mRequestingComplete = false;

    @Override
    public boolean onFragmentCreate() {
        HeymateEvents.register(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, this);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        HeymateEvents.unregister(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, this);
    }

    @Override
    public View createView(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.activity_attestation, null, false);

        mImageStep = content.findViewById(R.id.image_step);
        mTextTitle = content.findViewById(R.id.text_title);
        mTextDescription = content.findViewById(R.id.text_description);
        mInputCode = content.findViewById(R.id.input_code);
        mIndicatorStep = content.findViewById(R.id.indicator_step);
        mTextNext = content.findViewById(R.id.text_next);
        mImageNext = content.findViewById(R.id.image_next);
        mButtonNext = content.findViewById(R.id.button_next);

        setupTheme(content);

        mTextTitle.setText(Texts.get(Texts.ATTESTATION_CHECK_MESSAGES));
        mTextDescription.setText(Texts.get(Texts.ATTESTATION_CHECK_MESSAGES_DESCRIPTION));

        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle(Texts.get(Texts.YOUR_WALLET));
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    finishFragment();
                }
            }
        });

        mInputCode.setPinReceiver(pin -> {
            mPin = pin;

            updateState();
        });

        mButtonNext.setOnClickListener(v -> {
            mRequestingComplete = true;

            updateState();

            mWallet.completeAttestation(mPin, (success, verified, errorCause) -> {
                if (isFinishing()) {
                    return;
                }

                mRequestingComplete = false;

                mCanRequestMoreAttestations = true;

                if (success) {
                    mPin = null;
                    mInputCode.setPin("");
                }
                else {
                    // TODO Improve
                    CharSequence error = null;

                    switch (errorCause.getError()) {
                        case BAD_ATTESTATION_CODE:
                            error = Texts.get(Texts.ATTESTATION_BAD_CODE);
                            break;
                        case INVALID_ATTESTATION_CODE:
                            error = Texts.get(Texts.ATTESTATION_INVALID_CODE);
                            break;
                        case ATTESTATION_CODE_USED:
                            error = Texts.get(Texts.ATTESTATION_USED_CODE);
                            break;
                    }

                    if (error == null) {
                        if (errorCause.getMainCause().getError() == CeloError.NETWORK_ERROR) {
                            error = Texts.get(Texts.NETWORK_ERROR);
                        }
                        else {
                            error = errorCause.getError().getMessage();
                        }
                    }

                    Toast.makeText(mButtonNext.getContext(), error, Toast.LENGTH_LONG).show();
                }

                updateState();
            });
        });

        mWallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        updateState();

        return content;
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        switch (event) {
            case HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED:
                CeloException error = (CeloException) args[1];

                mCanUpdateVerifiedStatus = error != null && error.getMainCause().getError() == CeloError.NETWORK_ERROR;

                updateState();
                return;
        }
    }

    private void setupTheme(View content) {
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mTextTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mInputCode.setDigitColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mInputCode.setIndicatorColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
        mIndicatorStep.setMainColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
        mIndicatorStep.setOthersColor(Theme.getColor(Theme.key_divider));
        mTextNext.setTextColor(Theme.getColor(Theme.key_actionBarDefault));
        mImageNext.setColorFilter(Theme.getColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN);
        mButtonNext.setBackgroundColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
    }

    private void updateState() {
        VerifiedStatus verifiedStatus = mWallet.getVerifiedStatus();

        if (verifiedStatus == null) {
            setEnabled(false);

            if (mWallet.isCheckingVerifiedStatus()) {
                return;
            }

            if (mCanUpdateVerifiedStatus) {
                mWallet.updateVerifiedStatus();
            }
            else {
                // TODO Improve behavior
                Toast.makeText(mInputCode.getContext(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();

                finishFragment(true);
            }

            return;
        }

        if (verifiedStatus.verified) {
            presentFragment(new WalletActivity(), true);
            return;
        }

        switch (verifiedStatus.completedAttestations) {
            case 0:
                mImageStep.setImageResource(R.drawable.img_attestation_1);
                break;
            case 1:
                mImageStep.setImageResource(R.drawable.img_attestation_2);
                break;
            default:
                mImageStep.setImageResource(R.drawable.img_attestation_3);
                break;
        }

        mIndicatorStep.setIndex(verifiedStatus.completedAttestations, true);

        if (verifiedStatus.completedAttestations >= ATTESTATION_COUNT - 1) {
            mTextNext.setText(Texts.get(Texts.CONFIRM));
            mImageNext.setImageResource(R.drawable.ic_done);
        }
        else {
            mTextNext.setText(Texts.get(Texts.NEXT));
            mImageNext.setImageResource(R.drawable.msg_arrowright);
        }

        if (mHasPendingAttestationRequest || (verifiedStatus.totalAttestations < ATTESTATION_COUNT && requestAttestation()) || mRequestingComplete) {
            setEnabled(false);
            return;
        }

        setEnabled(true);
    }

    private boolean requestAttestation() {
        if (mHasPendingAttestationRequest || !mCanRequestMoreAttestations) {
            return false;
        }

        mHasPendingAttestationRequest = true;

        AlertDialog waitDialog = new AlertDialog.Builder(mButtonNext.getContext())
                .setTitle(Texts.get(Texts.ATTESTATION_REQUESTING))
                .setMessage(Texts.get(Texts.ATTESTATION_REQUESTING_MESSAGE))
                .setCancelable(false)
                .create();

        waitDialog.show();

        mWallet.requestAttestations((wallet, newAttestations, requiresMore, exception) -> {
            if (isFinishing()) {
                return;
            }

            mHasPendingAttestationRequest = false;

            waitDialog.dismiss();

            if (newAttestations == 0 && exception != null && exception.getMainCause().getError() == CeloError.NETWORK_ERROR) {
                mCanRequestMoreAttestations = false;

                Toast.makeText(mButtonNext.getContext(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();

                updateState();
            }
            else {
                // Done. Await the SMS
            }
        });

        return true;
    }

    private void setEnabled(boolean enabled) {
        mInputCode.setEnabled(enabled);

        boolean nextEnabled = enabled && mPin != null;

        mButtonNext.setEnabled(nextEnabled);
        mTextNext.setAlpha(nextEnabled ? 1f : 0.6f);
        mImageNext.setAlpha(nextEnabled ? 1f : 0.6f);
    }

}
