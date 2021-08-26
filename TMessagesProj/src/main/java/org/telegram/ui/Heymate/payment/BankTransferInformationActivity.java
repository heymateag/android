package org.telegram.ui.Heymate.payment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.Constants;
import org.telegram.ui.Heymate.HeymateRouter;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.TG2HM;

import works.heymate.beta.R;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.core.wallet.Wallet;
import works.heymate.ramp.HTLC;
import works.heymate.ramp.Peymate;

public class BankTransferInformationActivity extends BaseFragment implements Peymate.PaymentCheckListener {

    private static final String TAG = "BankTransferInfo";

    public static final String HOST = "bankTransfer";

    public static Intent getIntent(Context context, Money amount) {
        Bundle args = new Bundle();
        args.putParcelable(Constants.MONEY, amount);

        return HeymateRouter.createIntent(context, HOST, args);
    }

    private TextView mAmount;
    private TextView mIBAN;
    private TextView mBIC;
    private TextView mHolder;
    private TextView mDescription;

    private boolean mCheckRequested = false;

    public BankTransferInformationActivity(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context) {
        ActionBar actionBar = getActionBar();
        actionBar.setBackButtonDrawable(new BackDrawable(false));
        actionBar.setTitle("Bank Transfer"); // TODO TEXTS
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (getParentActivity() == null) {
                    return;
                }

                if (id == -1) {
                    Peymate.get(getParentActivity()).clear();
                    finishFragment();
                }
            }
        });

        View content = LayoutInflater.from(context).inflate(R.layout.activity_backtransferinformation, null, false);

        mAmount = content.findViewById(R.id.amount);
        TextView waitingTime = content.findViewById(R.id.waiting_time);
        View infoBackground = content.findViewById(R.id.info_background);
        TextView titleIBAN = content.findViewById(R.id.title_iban);
        mIBAN = content.findViewById(R.id.iban);
        TextView titleBIC = content.findViewById(R.id.title_bic);
        mBIC = content.findViewById(R.id.bic);
        TextView titleHolder = content.findViewById(R.id.title_holder);
        mHolder = content.findViewById(R.id.holder);
        TextView titleDescription = content.findViewById(R.id.title_description);
        mDescription = content.findViewById(R.id.description);
        TextView transferred = content.findViewById(R.id.transferred);
        TextView buttonTransferred = content.findViewById(R.id.button_transferred);

        setupCopy(content.findViewById(R.id.copy_iban), mIBAN);
        setupCopy(content.findViewById(R.id.copy_bic), mBIC);
        setupCopy(content.findViewById(R.id.copy_holder), mHolder);
        setupCopy(content.findViewById(R.id.copy_description), mDescription);

        mAmount.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        waitingTime.setTextColor(Theme.getColor(Theme.key_wallet_greenText));
        waitingTime.setText("The transfer process could take up to 3 minutes");

        infoBackground.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_windowBackgroundGray)));

        titleIBAN.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleIBAN.setText("IBAN");
        mIBAN.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        titleBIC.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleBIC.setText("BIC");
        mBIC.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        titleHolder.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleHolder.setText("Account holder");
        mHolder.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        titleDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        titleDescription.setText("Description");
        mDescription.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        transferred.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        transferred.setText("If you transfered money successfully click on:");

        buttonTransferred.setTextColor(Theme.getColor(Theme.key_chats_actionIcon));
        buttonTransferred.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(4), Theme.getColor(Theme.key_chats_actionBackground)));
        buttonTransferred.setText("Money Transferred");
        buttonTransferred.setOnClickListener(v -> {
            if (mCheckRequested) {
                return;
            }

            mCheckRequested = true;

            LoadingUtil.onLoadingStarted();

            Peymate.get(getParentActivity()).testClear();
            Peymate.get(getParentActivity()).check();
        });

        String phoneNumber = TG2HM.getCurrentPhoneNumber();
        Wallet wallet = Wallet.get(context, phoneNumber);

        LoadingUtil.onLoadingStarted();

        Money amount = getArguments().getParcelable(Constants.MONEY);

        Peymate.get(getParentActivity()).initiate(amount, wallet.getAddress(), (success, htlc, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (getParentActivity() == null) {
                return;
            }

            if (success) {
                Peymate.get(getParentActivity()).addListener(BankTransferInformationActivity.this);

                HTLC.Option option = htlc.clearing.options[0];

                long oAmount;
                String oIBAN;
                String oBIC;
                String oHolder;
                String oPurpose;

                if (option != null) {
                    oAmount = (int) (option.amount * 100);
                    oIBAN = option.recipient.iban;
                    oBIC = option.recipient.bic;
                    oHolder = option.recipient.name;
                    oPurpose = option.purpose;
                }
                else {
                    oAmount = amount.getCents();
                    oIBAN = "DE 24 1005 0000 1234 5678 90";
                    oBIC = "BELADEXX";
                    oHolder = "Heymate AG";
                    oPurpose = "ACX 10445 332 555 555 ";
                }

                String a = "Charge your wallet ";
                String b = " or more by sending money to heymate's account mentioned below.";
                String c = Money.create(oAmount, amount.getCurrency()).toString();

                SpannableStringBuilder sb = new SpannableStringBuilder();
                sb.append(a).append(c).append(b);
                sb.setSpan(new MetricAffectingSpan() {

                    @Override public void updateMeasureState(@NonNull TextPaint textPaint) { }

                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.setColor(Theme.getColor(Theme.key_wallet_greenText));
                    }

                }, a.length(), a.length() + c.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                mAmount.setText(sb);

                mIBAN.setText(oIBAN);
                mBIC.setText(oBIC);
                mHolder.setText(oHolder);
                mDescription.setText(oPurpose);
            }
            else {
                finishFragment();

                Log.e(TAG, "Failed to create nimiq request", exception);

                Toast.makeText(context, Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();
            }
        });

        fragmentView = content;

        return content;
    }

    private void setupCopy(ImageView view, TextView target) {
        Drawable icon = AppCompatResources.getDrawable(view.getContext(), R.drawable.hm_ic_copy);
        icon.setColorFilter(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText), PorterDuff.Mode.SRC_IN);
        view.setImageDrawable(icon);

        view.setOnClickListener(v -> copy(target));
    }

    private void copy(TextView text) {
        String str = text.getText().toString();

        ClipboardManager clipboard = (ClipboardManager) getParentActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(str, str);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(getParentActivity(), "Copied", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onPaymentCheckResult(boolean success, String status) {
        if (getParentActivity() == null) {
            return true;
        }

        if (mCheckRequested) {
            mCheckRequested = false;

            LoadingUtil.onLoadingFinished();
        }

        if (success && !HTLC.STATUS_PENDING.equals(status)) {
            Peymate.get(getParentActivity()).removeListener(this);

            getParentActivity().startActivity(BankTransferResultActivity.getIntent(getParentActivity()));

            finishFragment();
        }

        return false;
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();

        mIBAN = null;
        mBIC = null;
        mHolder = null;
        mDescription = null;

        super.clearViews();
    }

}
