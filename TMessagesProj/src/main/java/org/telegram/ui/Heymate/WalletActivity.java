package org.telegram.ui.Heymate;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.MetricAffectingSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;

import works.heymate.celo.CeloError;
import works.heymate.celo.CeloException;
import works.heymate.core.HeymateEvents;
import works.heymate.core.Texts;
import works.heymate.core.wallet.VerifiedStatus;
import works.heymate.core.wallet.Wallet;

public class WalletActivity extends BaseFragment implements HeymateEvents.HeymateEventObserver {

    private ImageView mImageWallet;
    private TextView mTextTitle;
    private TextView mTextStatus;
    private TextView mTextLeftButton;
    private ImageView mImageLeftButton;
    private View mLeftButton;
    private TextView mTextRightButton;
    private ImageView mImageRightButton;
    private View mRightButton;

    private Wallet mWallet;

    private boolean mCanUpdateVerifiedStatus = true;

    @Override
    public boolean onFragmentCreate() {
        HeymateEvents.register(HeymateEvents.WALLET_CREATED, this);
        HeymateEvents.register(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, this);

        return super.onFragmentCreate();
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();

        HeymateEvents.unregister(HeymateEvents.WALLET_CREATED, this);
        HeymateEvents.unregister(HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED, this);
    }

    @Override
    public View createView(Context context) {
        View content = LayoutInflater.from(context).inflate(R.layout.activity_wallet, null, false);

        mImageWallet = content.findViewById(R.id.image_wallet);
        mTextTitle = content.findViewById(R.id.text_title);
        mTextStatus = content.findViewById(R.id.text_status);
        mTextLeftButton = content.findViewById(R.id.text_leftbutton);
        mImageLeftButton = content.findViewById(R.id.image_leftbutton);
        mLeftButton = content.findViewById(R.id.leftbutton);
        mTextRightButton = content.findViewById(R.id.text_rightbutton);
        mImageRightButton = content.findViewById(R.id.image_rightbutton);
        mRightButton = content.findViewById(R.id.rightbutton);

        mTextStatus.setMovementMethod(LinkMovementMethod.getInstance());

        setupTheme(content);

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

        mLeftButton.setOnClickListener(v -> {
            if (mWallet.isCreating()) {
                return;
            }

            if (mWallet.isCreated()) {
                // TODO SECURE
            }
            else {
                mWallet.createNew();
                updateState();
            }
        });

        mRightButton.setOnClickListener(v -> {
            if (mWallet.isCreating()) {
                return;
            }

            if (mWallet.isCreated()) {
                // TODO Later
            }
            else {
                // TODO Improve
                EditText input = new EditText(v.getContext());
                input.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                input.setHint("Type your mnemonic");

                new AlertDialog.Builder(v.getContext())
                        .setTitle("Import Key")
                        .setView(input)
                        .setCancelable(false)
                        .setPositiveButton("Import", (dialog, which) -> {
                            String mnemonic = input.getText().toString();

                            if (mWallet.createFromMnemonic(mnemonic)) {
                                dialog.dismiss();
                            }
                            else {
                                Toast.makeText(input.getContext(), "Key is invalid.", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> {
                            dialog.dismiss();
                        })
                        .show();
            }
        });

        mWallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        updateState();

        return content;
    }

    @Override
    public void onHeymateEvent(int event, Object... args) {
        switch (event) {
            case HeymateEvents.WALLET_CREATED:
                if (mWallet == args[0]) {
                    mWallet.updateVerifiedStatus();
                    updateState();
                }
                return;
            case HeymateEvents.PHONE_NUMBER_VERIFIED_STATUS_UPDATED:
                CeloException error = (CeloException) args[2];

                mCanUpdateVerifiedStatus = error != null && error.getMainCause().getError() == CeloError.NETWORK_ERROR;

                updateState();
                return;
        }
    }

    private void setupTheme(View content) {
        content.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        mTextTitle.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mTextStatus.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        mTextLeftButton.setTextColor(Theme.getColor(Theme.key_actionBarDefault));
        mTextRightButton.setTextColor(Theme.getColor(Theme.key_actionBarDefault));
        mImageLeftButton.setColorFilter(Theme.getColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN);
        mImageRightButton.setColorFilter(Theme.getColor(Theme.key_actionBarDefault), PorterDuff.Mode.SRC_IN);
        content.findViewById(R.id.divider).setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        content.findViewById(R.id.bottombar).setBackgroundColor(ContextCompat.getColor(content.getContext(), R.color.ht_theme));
    }

    private void updateState() {
        if (!mWallet.isCreated()) {
            mImageWallet.setImageResource(R.drawable.img_no_wallet);
            mTextTitle.setText(Texts.get(Texts.NO_WALLET_DETECTED));
            mTextStatus.setText(Texts.get(Texts.NO_WALLET_DETECTED_MESSAGE));
            mTextLeftButton.setText(Texts.get(Texts.CREATE_NEW_WALLET));
            mTextRightButton.setText(Texts.get(Texts.IMPORT_EXISTING_WALLET));
            mImageLeftButton.setImageResource(R.drawable.group_edit);
            mImageRightButton.setImageResource(R.drawable.ic_import);

            if (mWallet.isCreating()) {
                mTextLeftButton.setAlpha(0.6f);
                mTextRightButton.setAlpha(0.6f);
                mImageLeftButton.setAlpha(0.6f);
                mImageRightButton.setAlpha(0.6f);
                mLeftButton.setEnabled(false);
                mRightButton.setEnabled(false);
            }
            else {
                mTextLeftButton.setAlpha(1f);
                mTextRightButton.setAlpha(1f);
                mImageLeftButton.setAlpha(1f);
                mImageRightButton.setAlpha(1f);
                mLeftButton.setEnabled(true);
                mRightButton.setEnabled(true);
            }
        }
        else {
            mImageWallet.setImageResource(R.drawable.img_checked_wallet);
            mTextTitle.setText(Texts.get(Texts.WALLET_DETECTED));

            String message = Texts.get(Texts.WALLET_DETECTED_MESSAGE).toString();

            int myKeyStartIndex = message.indexOf("__");

            if (myKeyStartIndex > 0) {
                final int themeColor = ContextCompat.getColor(mTextTitle.getContext(), R.color.ht_theme);

                int myKeyEndIndex = message.indexOf("__", myKeyStartIndex + 2);

                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(message.substring(0, myKeyStartIndex));
                ssb.append(message.substring(myKeyStartIndex + 2, myKeyEndIndex));
                ssb.setSpan(new MetricAffectingSpan() {

                    @Override
                    public void updateMeasureState(@NonNull TextPaint textPaint) {
                        textPaint.setUnderlineText(true);
                        textPaint.setColor(themeColor);
                    }

                    @Override
                    public void updateDrawState(TextPaint tp) {
                        tp.setUnderlineText(true);
                        tp.setColor(themeColor);
                    }

                }, myKeyStartIndex, myKeyEndIndex - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.setSpan(new ClickableSpan() {

                    @Override
                    public void onClick(@NonNull View widget) {
                        // TODO Improve
                        new AlertDialog.Builder(widget.getContext())
                                .setTitle("Your Key")
                                .setMessage(mWallet.getMnemonic())
                                .setNeutralButton("OK", (dialog, which) -> dialog.dismiss())
                                .show();
                    }

                }, myKeyStartIndex, myKeyEndIndex - 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mTextStatus.setText(ssb);
            }
            else {
                mTextStatus.setText(message);
            }

            mTextLeftButton.setText(Texts.get(Texts.SECURE));
            mTextRightButton.setText(Texts.get(Texts.LATER));
            mImageLeftButton.setImageResource(R.drawable.ic_secure);
            mImageRightButton.setImageResource(R.drawable.ic_later);

            VerifiedStatus verifiedStatus = mWallet.getVerifiedStatus();

            if (mWallet.isCheckingVerifiedStatus() || verifiedStatus == null) {
                mLeftButton.setEnabled(false);
                mRightButton.setEnabled(false);
                mTextLeftButton.setAlpha(0.6f);
                mTextRightButton.setAlpha(0.6f);
                mImageLeftButton.setAlpha(0.6f);
                mImageRightButton.setAlpha(0.6f);

                if (verifiedStatus == null) {
                    if (mCanUpdateVerifiedStatus) {
                        mWallet.updateVerifiedStatus();
                    }
                    else {
                        // TODO Improve behavior
                        Toast.makeText(mLeftButton.getContext(), Texts.get(Texts.NETWORK_ERROR), Toast.LENGTH_LONG).show();

                        finishFragment(true);
                    }
                }
            }
            else {
                if (!verifiedStatus.verified) {
                    // TODO NEED DESIGN
                    TextView addressView = new TextView(mLeftButton.getContext());
                    addressView.setText(mWallet.getAddress() + "\nhttps://celo.org/developers/faucet");
                    addressView.setAutoLinkMask(Linkify.WEB_URLS);
                    addressView.setTextIsSelectable(true);
                    addressView.setMovementMethod(LinkMovementMethod.getInstance());
                    new AlertDialog.Builder(mLeftButton.getContext())
                            .setTitle("Go to attestation?")
                            .setView(addressView)
                            .setPositiveButton("Go", (dialog, which) -> {
                                dialog.dismiss();
                                presentFragment(new AttestationActivity(), true);
                            })
                            .show();
                    return;
                }

                mLeftButton.setEnabled(true);
                mRightButton.setEnabled(true);
                mTextLeftButton.setAlpha(1f);
                mTextRightButton.setAlpha(1f);
                mImageLeftButton.setAlpha(1f);
                mImageRightButton.setAlpha(1f);
            }
        }
    }

    @Override
    public boolean onBackPressed() {
        finishFragment(true);
        return super.onBackPressed();
    }

}
