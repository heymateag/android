package org.telegram.ui.Heymate;

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
import works.heymate.core.wallet.AttestationRequestCallback;
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

        setupTheme();

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
            // TODO
        });

        mButtonNext.setOnClickListener(v -> {
            // TODO
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

    private void setupTheme() {
        View content = getFragmentView();

        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mTextTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mInputCode.setDigitColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mInputCode.setIndicatorColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
        mIndicatorStep.setMainColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
        mIndicatorStep.setOthersColor(Theme.getColor(Theme.key_divider));
        mTextNext.setTextColor(Theme.getColor(Theme.key_profile_actionIcon));
        mImageNext.setColorFilter(Theme.getColor(Theme.key_profile_actionIcon), PorterDuff.Mode.SRC_IN);
        mButtonNext.setBackgroundColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
    }

    private void updateState() {
        VerifiedStatus verifiedStatus = mWallet.getVerifiedStatus();

        if (verifiedStatus == null) {
            // TODO disable

            if (mWallet.isCheckingVerifiedStatus()) {
                return;
            }

            if (mCanUpdateVerifiedStatus) {
                mWallet.updateVerifiedStatus();
            }
            else {
                // TODO Improve behavior
                Toast.makeText(mInputCode.getContext(), Texts.get(Texts.WALLET_NETWORK_ERROR), Toast.LENGTH_LONG).show();

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

        if (verifiedStatus.totalAttestations < ATTESTATION_COUNT && !mHasPendingAttestationRequest) {
            // TODO disable

            requestAttestation();
            return;
        }

        // TODO
    }

    private void requestAttestation() {
        mHasPendingAttestationRequest = true;

        mWallet.requestAttestations((wallet, newAttestations, requiresMore, exception) -> {
            mHasPendingAttestationRequest = false;

            if (newAttestations == 0 && exception != null && exception.getMainCause().getError() == CeloError.NETWORK_ERROR) {
//                Toast.makeText()
                        // TODO
            }
        });
    }

}
