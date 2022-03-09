package org.telegram.ui.Heymate.user;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextWatcher;
import android.text.style.MetricAffectingSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.yashoid.sequencelayout.SequenceLayout;

import org.celo.contractkit.wrapper.ExchangeWrapper;
import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.RadialProgressView;
import org.telegram.ui.Heymate.HeymateRouter;
import org.telegram.ui.Heymate.TG2HM;
import org.telegram.ui.Heymate.payment.AwaitSettlement;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;

import works.heymate.api.APIArray;
import works.heymate.api.APIObject;
import works.heymate.beta.R;
import works.heymate.celo.CeloUtils;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Utils;
import works.heymate.core.wallet.AwaitBalance;
import works.heymate.core.wallet.Prices;
import works.heymate.core.wallet.Wallet;
import works.heymate.model.User;
import works.heymate.model.Users;
import works.heymate.ramp.Ramp;

public class SendMoneySheet extends BottomSheet {

    private static final String TAG = "SendMoneySheet";

    private static final BigInteger TRANSACTION_COST = CurrencyUtil.WEI.divide(CurrencyUtil.ONE_HUNDRED).divide(CurrencyUtil.ONE_HUNDRED);

    public static final String HOST = "SendMoney";

    private static final String KEY_RECEIVER_ID = "r_i";
    private static final String KEY_WALLET_ADDRESS = "w_a";
    private static final String KEY_SEND_AMOUNT = "s_a";
    private static final String KEY_SEND_CURRENCY = "s_c";

    private static final int STATE_IDLE = 0;
    private static final int STATE_SENDING = 1;
    private static final int STATE_SUCCESS = 2;
    private static final int STATE_FAILURE = 3;

    private SequenceLayout mLayout;
    private TextView mTitleSend;
    private TextView mCurrencySend;
    private View mInputSendBackground;
    private EditText mInputSend;
    private TextView mTitleReceive;
    private TextView mCurrencyReceive;
    private View mInputReceiveBackground;
    private EditText mInputReceive;
    private View mArrow;
    private TextView mExchange;
    private TextView mTitleBalance;
    private TextView mBalance;
    private TextView mBalanceError;
    private EditText mDescription;
    private TextView mButton;
    private View mLoadingBackground;
    private RadialProgressView mLoading;
    private TextView mLoadingText;
    private View mImageResult;
    private TextView mTextResult;

    private final Currency mFromCurrency;
    private Currency mToCurrency = null;

    private final Money mReferenceFrom;
    private Money mReferenceTo;

    private Money mTotalBalance;

    private final Money mSendAmount;
    private Money mReceiveAmount = null;

    private APIObject mReceiver = null;
    private String mReceiverWalletAddress = null;

    private int mState = STATE_IDLE;
    private boolean mUpdatingConversion = false;

    public SendMoneySheet(Context context, Uri data) {
        this(context);

        String receiverId = data.getQueryParameter(KEY_RECEIVER_ID);
        setReceiverWalletAddress(data.getQueryParameter(KEY_WALLET_ADDRESS), true);
        long sendAmount = Long.parseLong(data.getQueryParameter(KEY_SEND_AMOUNT));
        setToCurrency(Currency.forName(data.getQueryParameter(KEY_SEND_CURRENCY)));

        mSendAmount.setCents(sendAmount);
        mInputSend.setText(String.valueOf(sendAmount / 100f));

        if (receiverId != null) {
            Users.getUser(receiverId, result -> {
                if (result.response != null) {
                    setReceiver(result.response);
                } else {
                    dismiss();
                }
            });
        }
    }

    public SendMoneySheet(Context context, String walletAddress) {
        this(context);

        mReceiverWalletAddress = walletAddress;
    }

    public SendMoneySheet(Context context) {
        super(context, true);

        setDimBehindAlpha(0x4D);
        setDimBehind(true);
        setTitle("Send money", true);

        mLayout = (SequenceLayout) LayoutInflater.from(context).inflate(R.layout.sheet_sendmoney, null, false);

        mTitleSend = mLayout.findViewById(R.id.title_send);
        mCurrencySend = mLayout.findViewById(R.id.input_send_currency);
        mInputSendBackground = mLayout.findViewById(R.id.input_send_background);
        mInputSend = mLayout.findViewById(R.id.input_send);
        mTitleReceive = mLayout.findViewById(R.id.title_receive);
        mCurrencyReceive = mLayout.findViewById(R.id.input_receive_currency);
        mInputReceiveBackground = mLayout.findViewById(R.id.input_receive_background);
        mInputReceive = mLayout.findViewById(R.id.input_receive);
        mArrow = mLayout.findViewById(R.id.arrow);
        mExchange = mLayout.findViewById(R.id.exchange);
        mTitleBalance = mLayout.findViewById(R.id.balance_title);
        mBalance = mLayout.findViewById(R.id.balance);
        mBalanceError = mLayout.findViewById(R.id.balance_error);
        mDescription = mLayout.findViewById(R.id.description);
        mButton = mLayout.findViewById(R.id.send);
        mLoadingBackground = mLayout.findViewById(R.id.loading_background);
        mLoading = mLayout.findViewById(R.id.loading);
        mLoadingText = mLayout.findViewById(R.id.loading_text);
        mImageResult = mLayout.findViewById(R.id.image_result);
        mTextResult = mLayout.findViewById(R.id.text_result);

        setupText(mTitleSend, "You send");
        setupText(mTitleReceive, "They receive");
        setupText(mExchange, "");
        setupText(mTitleBalance, "Your wallet balance:");
        setupText(mLoadingText, "Sending ... \uD83E\uDD11");
        setupInput(mInputSend, "00.00");
        setupInput(mInputReceive, "00.00");
        setupInput(mDescription, "Description (optional)");

        mBalance.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));

        mBalanceError.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteRedText));
        mBalanceError.setText("Not enough balance");

        Drawable box = Theme.createRoundRectDrawable(AndroidUtilities.dp(6), Theme.getColor(Theme.key_windowBackgroundGray));
        mInputSendBackground.setBackground(box);
        mInputReceiveBackground.setBackground(box);
        mDescription.setBackground(box);

        mLoadingBackground.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(18), Theme.getColor(Theme.key_windowBackgroundGray)));

        Drawable arrow = Theme.getThemedDrawable(context, R.drawable.hm_sendmoney_arrow, Theme.key_windowBackgroundWhiteBlueIcon);
        mArrow.setBackground(arrow);

        setCustomView(mLayout);

        mFromCurrency = TG2HM.getDefaultCurrency();
        mReferenceFrom = Money.create(100_00, mFromCurrency);

        mSendAmount = Money.create(0, mFromCurrency);

        mCurrencySend.setText(mFromCurrency.symbol());

        updateStateViews();

        mCurrencySend.setOnClickListener(v -> {
            new AlertDialog.Builder(getContext())
                    .setTitle("Change currency")
                    .setMessage("Select a currency for the receiver.")
                    .setItems(Currency.CURRENCY_NAMES, (dialog, which) -> {
                        setToCurrency(Currency.forName(Currency.CURRENCY_NAMES[which]));
                        updateStateViews();
                    })
                    .show();
        });

        mInputSend.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (mUpdatingConversion) {
                    return;
                }

                long cents = s.length() == 0 ? 0 : (long) (Float.parseFloat(s.toString()) * 100);
                mSendAmount.setCents(cents);

                checkHasError();

                if (mReferenceTo != null) {
                    updateReceiveMoney();
                }
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

        });

        mInputReceive.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                if (mUpdatingConversion) {
                    return;
                }

                mUpdatingConversion = true;

                long cents = s.length() == 0 ? 0 : (long) (Float.parseFloat(s.toString()) * 100);
                mReceiveAmount = Money.create(cents, mToCurrency);

                long sendCents = cents * mReferenceFrom.getCents() / mReferenceTo.getCents();
                mSendAmount.setCents(sendCents);

                mInputSend.setText(String.valueOf(sendCents / 100f));

                checkHasError();

                mUpdatingConversion = false;
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

        });

        mButton.setOnClickListener(v -> {
            switch (mState) {
                case STATE_IDLE:
                case STATE_FAILURE:
                    if (mSendAmount.getCents() == 0 || mReceiverWalletAddress == null || mTotalBalance == null) {
                        return;
                    }

                    if (mReceiveAmount == null) {
                        doSimpleSend();
                    }
                    else {
                        doExchangeSend();
                    }
                    return;
                case STATE_SUCCESS:
                    dismiss();
                    return;
            }
        });
    }

    private void doSimpleSend() {
        mState = STATE_SENDING;
        updateStateViews();

        final Money send = mSendAmount;
        final String targetWallet = mReceiverWalletAddress;

        final Wallet wallet = TG2HM.getWallet();

        wallet.getContractKit((success, contractKit, errorCause) -> {
            if (contractKit != null) {
                StableTokenWrapper token = CeloUtils.getToken(contractKit, send.getCurrency());

                try {
                    BigInteger balance = token.balanceOf(wallet.getAddress()).send();
                    BigInteger sendAmount = CurrencyUtil.centsToBlockChainValue(send.getCents());
                    BigInteger requiredBalance = sendAmount.add(TRANSACTION_COST);

                    if (balance.compareTo(requiredBalance) >= 0) {
                        CeloUtils.adjustGasPayment(contractKit, send.getCurrency());

                        try {
                            TransactionReceipt receipt = token.transfer(targetWallet, sendAmount).send();

                            Log.i(TAG, "Transfer successful: " + receipt.getTransactionHash());

                            Utils.postOnUIThread(() -> {
                                mState = STATE_SUCCESS;
                                updateStateViews();
                            });
                        } catch (Exception transferException) {
                            Log.e(TAG, "Transfer failed", transferException);

                            Utils.postOnUIThread(() -> {
                                mState = STATE_FAILURE;
                                updateStateViews();
                            });
                        }
                    }
                    else {
                        BigInteger missingAmount = requiredBalance.add(balance.negate());
                        BigInteger oneCent = CurrencyUtil.centsToBlockChainValue(1);
                        BigInteger topUpAmount = missingAmount.max(oneCent);

                        Utils.postOnUIThread(() -> {
                            Ramp.init(wallet.getAddress(), Money.create(CurrencyUtil.blockChainValueToCents(topUpAmount), send.getCurrency()), successful -> {
                                if (successful) {
                                    AwaitBalance.on(wallet, send.getCurrency(), onRampSuccess -> {
                                        if (onRampSuccess) {
                                            doSimpleSend();
                                        }
                                        else {
                                            dismiss();

                                            new AlertDialog.Builder(getContext())
                                                    .setTitle("Money not yet in wallet")
                                                    .setMessage("Heymate will monitor for it's arrival and show you a notification to resume later.")
                                                    .setPositiveButton("Ok", (dialog, which) -> {
                                                        AwaitSettlement.initiate(
                                                                getContext(),
                                                                token.getContractAddress(),
                                                                createUri(),
                                                                "Money arrived in your wallet",
                                                                "Click to continue to send money to " + mReceiver.getString(User.FULL_NAME));
                                                    })
                                                    .show();
                                        }
                                    });
                                }
                                else {
                                    mState = STATE_IDLE;
                                    updateStateViews();
                                }
                            });
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to prepare to transfer", e);

                    Utils.postOnUIThread(() -> {
                        mState = STATE_FAILURE;
                        updateStateViews();
                    });
                }
            }
            else {
                Log.e(TAG, "Failed to get contract kit", errorCause);

                Utils.postOnUIThread(() -> {
                    mState = STATE_FAILURE;
                    updateStateViews();
                });
            }
        });
    }

    private void doExchangeSend() {
        mState = STATE_SENDING;
        updateStateViews();

        final Money send = mSendAmount;
        final Money receive = mReceiveAmount;
        final String targetWallet = mReceiverWalletAddress;

        final Wallet wallet = TG2HM.getWallet();

        wallet.getContractKit((success, contractKit, errorCause) -> {
            if (success) {
                StableTokenWrapper sendToken = CeloUtils.getToken(contractKit, send.getCurrency());
                StableTokenWrapper receiveToken = CeloUtils.getToken(contractKit, receive.getCurrency());

                ExchangeWrapper sendExchange = CeloUtils.getExchange(contractKit, send.getCurrency());
                ExchangeWrapper receiveExchange = CeloUtils.getExchange(contractKit, receive.getCurrency());

                BigInteger receiveAmount = CurrencyUtil.centsToBlockChainValue(receive.getCents());

                try {
                    BigInteger cautionAmount = Convert.toWei(BigDecimal.ONE, Convert.Unit.ETHER).toBigInteger().divide(BigInteger.valueOf(2000));

                    BigInteger goldToSell = receiveExchange.getSellTokenAmount(receiveAmount.add(cautionAmount), true).send();
                    BigInteger nativeToSell = sendExchange.getSellTokenAmount(goldToSell, false).send();

                    BigInteger requiredBalance = nativeToSell
                            .add(cautionAmount)
                            .add(TRANSACTION_COST) // approve to buy gold
                            .add(TRANSACTION_COST) // buy gold
                            .add(TRANSACTION_COST) // approve to but target amount
                            .add(TRANSACTION_COST) // buy target amount
                            .add(TRANSACTION_COST); // final transfer

                    BigInteger balance = sendToken.balanceOf(wallet.getAddress()).send();

                    if (balance.compareTo(requiredBalance) >= 0) {
                        try {
                            CeloUtils.adjustGasPayment(contractKit, send.getCurrency());

                            sendToken.approve(sendExchange.getContractAddress(), nativeToSell.add(cautionAmount)).send();
                            sendExchange.buy(goldToSell, nativeToSell.add(cautionAmount), true).send();

                            contractKit.contracts.getGoldToken().approve(receiveExchange.getContractAddress(), goldToSell.add(cautionAmount)).send();
                            receiveExchange.buy(receiveAmount, goldToSell.add(cautionAmount), false).send();

                            TransactionReceipt receipt = receiveToken.transfer(targetWallet, receiveAmount).send();

                            Log.i(TAG, "Transfer successful: " + receipt.getTransactionHash());

                            Utils.postOnUIThread(() -> {
                                mState = STATE_SUCCESS;
                                updateStateViews();
                            });
                        } catch (Exception transferException) {
                            Log.e(TAG, "Transfer failed", transferException);

                            Utils.postOnUIThread(() -> {
                                mState = STATE_FAILURE;
                                updateStateViews();
                            });
                        }
                    }
                    else {
                        BigInteger missingAmount = requiredBalance.add(balance.negate());
                        BigInteger oneCent = CurrencyUtil.centsToBlockChainValue(1);
                        BigInteger topUpAmount = missingAmount.max(oneCent);

                        Utils.postOnUIThread(() -> {
                            Ramp.init(wallet.getAddress(), Money.create(CurrencyUtil.blockChainValueToCents(topUpAmount), send.getCurrency()), successful -> {
                                if (successful) {
                                    AwaitBalance.on(wallet, send.getCurrency(), onRampSuccess -> {
                                        if (onRampSuccess) {
                                            doExchangeSend();
                                        }
                                        else {
                                            dismiss();

                                            new AlertDialog.Builder(getContext())
                                                    .setTitle("Money not yet in wallet")
                                                    .setMessage("Heymate will monitor for it's arrival and show you a notification to resume later.")
                                                    .setPositiveButton("Ok", (dialog, which) -> {
                                                        AwaitSettlement.initiate(
                                                                getContext(),
                                                                sendToken.getContractAddress(),
                                                                createUri(),
                                                                "Money arrived in your wallet",
                                                                "Click to continue to send money to " + mReceiver.getString(User.FULL_NAME));
                                                    })
                                                    .show();
                                        }
                                    });
                                }
                                else {
                                    mState = STATE_IDLE;
                                    updateStateViews();
                                }
                            });
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Failed to prepare to transfer", e);

                    Utils.postOnUIThread(() -> {
                        mState = STATE_FAILURE;
                        updateStateViews();
                    });
                }
            }
            else {
                Log.e(TAG, "Failed to get contract kit", errorCause);

                Utils.postOnUIThread(() -> {
                    mState = STATE_FAILURE;
                    updateStateViews();
                });
            }
        });
    }

    private void checkHasError() {
        boolean hasError = mTotalBalance != null && mSendAmount.getCents() > 0 && mTotalBalance.getCents() < mSendAmount.getCents();

        setHasError(hasError);
        updateButton("Send", true);
    }

    private void updateReceiveMoney() {
        mUpdatingConversion = true;

        long receiveCents = mSendAmount.getCents() * mReferenceTo.getCents() / mReferenceFrom.getCents();
        mReceiveAmount = Money.create(receiveCents, mToCurrency);

        mInputReceive.setText(String.valueOf(receiveCents / 100f));

        mUpdatingConversion = false;
    }

    public boolean setReceiver(APIObject user) {
        mReceiver = user;

        if (mReceiver == null) {
            setToCurrency(null);
        }
        else {
            setTitle("Send to " + user.getString(User.FULL_NAME), true);

            updateReceiverCurrencyAndWallet();

            if (mToCurrency != null) {
                mCurrencyReceive.setText(mToCurrency.symbol());
            }
            else {
                return false;
            }
        }

        updateStateViews();

        return true;
    }

    private void updateReceiverCurrencyAndWallet() {
        mToCurrency = null;
        mReceiverWalletAddress = null;

        if (mReceiver == null) {
            return;
        }

        APIArray devices = mReceiver.getArray(User.DEVICES);

        if (devices == null || devices.size() == 0) {
            return;
        }

        APIObject device = devices.getObject(0);

        if (device == null) {
            if (mReceiverWalletAddress == null) {
                Toast.makeText(getContext(), "User's wallet not found.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        String currencyName = device.getString(User.Device.CURRENCY);
        String walletAddress = device.getString(User.Device.WALLET_ADDRESS);

        if (currencyName == null || (walletAddress == null && mReceiverWalletAddress == null)) {
            return;
        }

        setToCurrency(Currency.forName(currencyName));
        setReceiverWalletAddress(walletAddress, false);
    }

    private void setToCurrency(Currency currency) {
        mToCurrency = currency;

        if (mToCurrency != null) {
            mCurrencyReceive.setText(mToCurrency.symbol());
        }
    }

    private void setReceiverWalletAddress(String walletAddress, boolean override) {
        if (mReceiverWalletAddress != null && !override) {
            return;
        }

        mReceiverWalletAddress = walletAddress;

        if (mDescription.length() == 0) {
            if (mReceiver != null) {
                mDescription.setText("Hi " + mReceiver.getString(User.FULL_NAME) + ", here is my payment to your wallet address: " + mReceiverWalletAddress);
            }
            else {
                mDescription.setText("Hi, here is my payment to your wallet address: " + mReceiverWalletAddress);
            }
        }
    }

    private void updateStateViews() {
        if (isDismissed()) {
            return;
        }

        setCanDismissWithSwipe(true);
        setCancelable(true);

        switch (mState) {
            case STATE_IDLE:
                checkHasError();

                if (mFromCurrency == null || mToCurrency == null || mFromCurrency.equals(mToCurrency)) {
                    layoutToMainSameCurrency();
                }
                else {
                    layoutToMainDifferentCurrency();

                    Prices.get(TG2HM.getWallet(), mReferenceFrom, mToCurrency, money -> {
                        if (!mReferenceFrom.equals(money)) {
                            mReferenceTo = money;

                            updateReceiveMoney();

                            mExchange.setText(mReferenceFrom.multiplyBy(0.01f) + " = " + mReferenceTo.multiplyBy(0.01f));
                        }
                        else {
                            Log.e(TAG, "Failed to get conversion rate.");
                            dismiss();
                        }
                    });
                }
                return;
            case STATE_SENDING:
                setCanDismissWithSwipe(false);
                setCancelable(false);

                layoutToLoading();
                return;
            case STATE_SUCCESS:
                layoutToResult(mSendAmount, mReceiveAmount, mReceiver == null ? null : mReceiver.getString(User.FULL_NAME));
                updateButton("Okay", true);
                return;
            case STATE_FAILURE:
                layoutToResult(null, null, null);
                updateButton("Try again", true);
                return;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        updateStateViews();
        checkBalance();
        checkHasError();
    }

    private void checkBalance() {
        TG2HM.getWallet().getBalance((success, usdBalance, eurBalance, realBalance, errorCause) -> {
            if (success) {
                mTotalBalance = TG2HM.pickTheRightMoney(usdBalance, eurBalance, realBalance);

                mBalance.setText(mTotalBalance.toString());
            }
            else {
                Log.e(TAG, "Failed to query balance", errorCause);

                mBalance.setText("Error!");
            }

            checkHasError();
        });
    }

    private void layoutToMainSameCurrency() {
        mLayout.findSequenceById("spine").getSpans().get(0).size = 352;
        mLayout.findSequenceById("receive").getSpans().get(3).size = 0;
        mLayout.requestLayout();

        mCurrencySend.setVisibility(View.VISIBLE);
        mInputSend.setVisibility(View.VISIBLE);
        mInputSendBackground.setVisibility(View.VISIBLE);
        mTitleBalance.setVisibility(View.VISIBLE);
        mBalance.setVisibility(View.VISIBLE);
        mBalanceError.setVisibility(View.VISIBLE);
        mDescription.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.VISIBLE);

        mTitleSend.setVisibility(View.GONE);
        mTitleReceive.setVisibility(View.GONE);
        mCurrencyReceive.setVisibility(View.GONE);
        mInputReceive.setVisibility(View.GONE);
        mInputReceiveBackground.setVisibility(View.GONE);
        mArrow.setVisibility(View.GONE);
        mExchange.setVisibility(View.GONE);

        mLoadingBackground.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);

        mTextResult.setVisibility(View.GONE);
        mImageResult.setVisibility(View.GONE);
    }

    private void layoutToMainDifferentCurrency() {
        mLayout.findSequenceById("spine").getSpans().get(0).size = 400;
        mLayout.findSequenceById("receive").getSpans().get(3).size = 1;
        mLayout.requestLayout();

        mCurrencySend.setVisibility(View.VISIBLE);
        mInputSend.setVisibility(View.VISIBLE);
        mInputSendBackground.setVisibility(View.VISIBLE);
        mTitleBalance.setVisibility(View.VISIBLE);
        mBalance.setVisibility(View.VISIBLE);
        mBalanceError.setVisibility(View.VISIBLE);
        mDescription.setVisibility(View.VISIBLE);
        mButton.setVisibility(View.VISIBLE);

        mTitleSend.setVisibility(View.VISIBLE);
        mTitleReceive.setVisibility(View.VISIBLE);
        mCurrencyReceive.setVisibility(View.VISIBLE);
        mInputReceive.setVisibility(View.VISIBLE);
        mInputReceiveBackground.setVisibility(View.VISIBLE);
        mArrow.setVisibility(View.VISIBLE);
        mExchange.setVisibility(View.VISIBLE);

        mLoadingBackground.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);

        mTextResult.setVisibility(View.GONE);
        mImageResult.setVisibility(View.GONE);
    }

    private void layoutToLoading() {
        mLayout.findSequenceById("spine").getSpans().get(0).size = 376;
        mLayout.requestLayout();

        mCurrencySend.setVisibility(View.GONE);
        mInputSend.setVisibility(View.GONE);
        mInputSendBackground.setVisibility(View.GONE);
        mTitleBalance.setVisibility(View.GONE);
        mBalance.setVisibility(View.GONE);
        mBalanceError.setVisibility(View.GONE);
        mDescription.setVisibility(View.GONE);
        mButton.setVisibility(View.GONE);

        mTitleSend.setVisibility(View.GONE);
        mTitleReceive.setVisibility(View.GONE);
        mCurrencyReceive.setVisibility(View.GONE);
        mInputReceive.setVisibility(View.GONE);
        mInputReceiveBackground.setVisibility(View.GONE);
        mArrow.setVisibility(View.GONE);
        mExchange.setVisibility(View.GONE);

        mLoadingBackground.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.VISIBLE);
        mLoadingText.setVisibility(View.VISIBLE);

        mTextResult.setVisibility(View.GONE);
        mImageResult.setVisibility(View.GONE);
    }

    private void layoutToResult(Money sent, Money received, String name) {
        mLayout.findSequenceById("spine").getSpans().get(0).size = 336;
        mLayout.requestLayout();

        mCurrencySend.setVisibility(View.GONE);
        mInputSend.setVisibility(View.GONE);
        mInputSendBackground.setVisibility(View.GONE);
        mTitleBalance.setVisibility(View.GONE);
        mBalance.setVisibility(View.GONE);
        mBalanceError.setVisibility(View.GONE);
        mDescription.setVisibility(View.GONE);
        mButton.setVisibility(View.VISIBLE);

        mTitleSend.setVisibility(View.GONE);
        mTitleReceive.setVisibility(View.GONE);
        mCurrencyReceive.setVisibility(View.GONE);
        mInputReceive.setVisibility(View.GONE);
        mInputReceiveBackground.setVisibility(View.GONE);
        mArrow.setVisibility(View.GONE);
        mExchange.setVisibility(View.GONE);

        mLoadingBackground.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
        mLoadingText.setVisibility(View.GONE);

        mTextResult.setVisibility(View.VISIBLE);
        mImageResult.setVisibility(View.VISIBLE);

        if (sent != null) {
            mImageResult.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.hm_sendmoney_success));

            String content = "You successfully sent {amount} to {name}.".replace("{name}", name);
            final int amountIndex = content.indexOf("{amount}");

            final String sentStr = sent.toString();

            if (received != null) {
                final String receivedStr = received.toString();

                String amount = sentStr + " ~ " + receivedStr;

                SpannableStringBuilder text = new SpannableStringBuilder(content.replace("{amount}", amount));

                text.setSpan(newSpan(Theme.key_windowBackgroundWhiteBlueText), amountIndex, amountIndex + sentStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                text.setSpan(newSpan(Theme.key_windowBackgroundWhiteBlueText), amountIndex + sentStr.length() + 3, amountIndex + sentStr.length() + 3 + receivedStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                mTextResult.setText(text);
            }
            else {
                SpannableStringBuilder text = new SpannableStringBuilder(content.replace("{amount}", sentStr));

                text.setSpan(newSpan(Theme.key_windowBackgroundWhiteBlueText), amountIndex, amountIndex + sentStr.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                mTextResult.setText(text);
            }
        }
        else {
            mImageResult.setBackground(AppCompatResources.getDrawable(getContext(), R.drawable.hm_sendmoney_failure));

            SpannableStringBuilder text = new SpannableStringBuilder("Unfortunately your payment failed,\nPlease try again.");

            text.setSpan(newSpan(Theme.key_windowBackgroundWhiteRedText), 27, 33, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            mTextResult.setText(text);
        }
    }

    private void setHasError(boolean hasError) {
        int color = Theme.getColor(hasError ? Theme.key_windowBackgroundWhiteRedText : Theme.key_windowBackgroundWhiteBlackText);

        mCurrencySend.setTextColor(color);
        mCurrencyReceive.setTextColor(color);
        mInputSend.setTextColor(color);
        mInputReceive.setTextColor(color);

        mBalanceError.setVisibility(hasError ? View.VISIBLE : View.INVISIBLE);
    }

    private void updateButton(String text, boolean enabled) {
        mButton.setText(text); // TODO texts
        mButton.setEnabled(enabled);
        mButton.setTextColor(Theme.getColor(Theme.key_dialogFloatingIcon));
        mButton.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(enabled ? Theme.key_dialogFloatingButton : Theme.key_windowBackgroundWhiteGrayIcon)));
    }

    private void setupText(TextView textView, String textKey) {
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setText(textKey); // TODO texts
    }

    private void setupInput(EditText input, String hint) {
        input.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        input.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        input.setHint(hint); // TODO texts
    }

    private Uri createUri() {
        StringBuilder url = new StringBuilder(HeymateRouter.INTERNAL_SCHEME + "://" + HOST + "?");

        if (mReceiver != null) {
            url.append(KEY_RECEIVER_ID).append("=").append(mReceiver.getString(User.ID)).append("&");
        }

        url.append(KEY_WALLET_ADDRESS).append("=").append(mReceiverWalletAddress).append("&");
        url.append(KEY_SEND_AMOUNT).append("=").append(mSendAmount.getCents()).append("&");
        url.append(KEY_SEND_CURRENCY).append("=").append(mToCurrency == null ? mFromCurrency.name() : mToCurrency.name());

        return Uri.parse(url.toString());
    }

    private static Object newSpan(final String colorCode) {
        final int color = Theme.getColor(colorCode);

        return new MetricAffectingSpan() {

            @Override
            public void updateMeasureState(@NonNull TextPaint textPaint) {
                textPaint.setColor(color);
            }

            @Override
            public void updateDrawState(TextPaint tp) {
                tp.setColor(color);
            }

        };
    }

}
