package org.telegram.ui.Heymate.wallet;

import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.celo.contractkit.wrapper.StableTokenWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.LoadingUtil;
import org.telegram.ui.Heymate.TG2HM;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import works.heymate.beta.R;
import works.heymate.celo.CurrencyUtil;
import works.heymate.celo.InternalUtils;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;
import works.heymate.ramp.alphafortress.AlphaFortressness;
import works.heymate.ramp.alphafortress.BeneficiaryModel;

public class WalletActivity extends BaseFragment {

    private static final String TAG = "WalletActivity";

    private TextView mTextBalance;

    private TransactionAdapter mAdapter;

    private Wallet mWallet;

    private String mUSDAddress;
    private String mEURAddress;

    @Override
    public boolean onFragmentCreate() {
        return super.onFragmentCreate();
    }

    @Override
    public View createView(Context context) {
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

        View content = LayoutInflater.from(context).inflate(R.layout.activity_wallet, null, false);

        TextView title = content.findViewById(R.id.title_balance);
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        title.setText("Total balance");

        mTextBalance = content.findViewById(R.id.balance);
        mTextBalance.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

        TextView cashOut = content.findViewById(R.id.cashout);
        cashOut.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton));
        cashOut.setBackground(Theme.createBorderRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton)));
        cashOut.setText("Cash Out");
        cashOut.setOnClickListener(view -> {
            if (AlphaFortressness.hasPendingTransaction()) {
                LoadingUtil.onLoadingStarted();

                AlphaFortressness.getPendingTransaction((success, transaction, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (transaction != null) {
                        if (transaction.transactionHash == null) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Cash out error")
                                    .setMessage("There has been an unexpected error during the cash out. Please contact heymate to follow up.")
                                    .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setNegativeButton("Issue resolved", (dialogInterface, i) -> {
                                        AlphaFortressness.clearPendingTransaction();
                                        dialogInterface.dismiss();
                                    })
                                    .show();
                        }
                        else if (transaction.isPaid == null) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Cash out in progress")
                                    .setMessage("Cash out is in progress.")
                                    .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .show();
                        } else if (transaction.isPaid) {
                            new AlertDialog.Builder(context)
                                    .setTitle("Cash out completed")
                                    .setMessage("The cash out has been completed. The money is in your account.")
                                    .setNeutralButton("OK", (dialogInterface, i) -> {
                                        AlphaFortressness.clearPendingTransaction();
                                        dialogInterface.dismiss();
                                    })
                                    .show();
                        }
                        else {
                            new AlertDialog.Builder(context)
                                    .setTitle("Cash out failed")
                                    .setMessage("There has been a problem with the cash out. Please contact heymate to follow up.")
                                    .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                    .setNegativeButton("Issue resolved", (dialogInterface, i) -> {
                                        AlphaFortressness.clearPendingTransaction();
                                        dialogInterface.dismiss();
                                    })
                                    .show();
                        }
                    }
                    else {
                        new AlertDialog.Builder(context)
                                .setTitle("Error")
                                .setMessage("Failed to retrieve the ongoing cash out.")
                                .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                                .show();
                    }
                });
            }
            else {
                LoadingUtil.onLoadingStarted();

                AlphaFortressness.getConversionRate(TG2HM.getDefaultCurrency(), (success, rate, exception) -> {
                    LoadingUtil.onLoadingFinished();

                    if (rate != null && rate > 0) {
                        Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

                        LoadingUtil.onLoadingStarted();

                        wallet.getContractKit((success1, contractKit, errorCause) -> {
                            LoadingUtil.onLoadingFinished();

                            if (contractKit != null) {
                                StableTokenWrapper token;

                                if (Currency.USD.equals(TG2HM.getDefaultCurrency())) {
                                    token = contractKit.contracts.getStableToken();
                                }
                                else if (Currency.EUR.equals(TG2HM.getDefaultCurrency())) {
                                    token = contractKit.contracts.getStableTokenEUR();
                                }
                                else {
                                    Utils.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Unsupported currency", Toast.LENGTH_LONG).show());
                                    return;
                                }

                                try {
                                    BigInteger balance = token.balanceOf(contractKit.getAddress()).send();

                                    BigInteger maximumAmount = AlphaFortressness.getConvertibleAmount(balance, rate);

                                    if (maximumAmount.compareTo(BigInteger.ZERO) <= 0) {
                                        Utils.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Balance is not enough for cash out", Toast.LENGTH_LONG).show());
                                        return;
                                    }

                                    Utils.runOnUIThread(() -> cashOutConfirmAmount(maximumAmount, rate));
                                } catch (Exception e) {
                                    Utils.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Failed to talk to node", Toast.LENGTH_LONG).show());
                                }
                            }
                            else {
                                Utils.runOnUIThread(() -> Toast.makeText(getParentActivity(), "Failed to connect to node", Toast.LENGTH_LONG).show());
                            }
                        });
                    }
                    else {
                        Toast.makeText(getParentActivity(), "Failed to query conversion rate", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        TextView addMoney = content.findViewById(R.id.add_money);
        addMoney.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhite));
        addMoney.setBackground(Theme.createRoundRectDrawable(AndroidUtilities.dp(8), Theme.getColor(Theme.key_windowBackgroundWhiteBlueButton)));
        addMoney.setText("Add Money");

        content.findViewById(R.id.spacer).setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));

        TextView titleTransactions = content.findViewById(R.id.title_transactions);
        titleTransactions.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteGrayText));
        titleTransactions.setText("Transaction");

        RecyclerView transactionList = content.findViewById(R.id.list_transaction);
        transactionList.setLayoutManager(new LinearLayoutManager(context));

        mAdapter = new TransactionAdapter();
        transactionList.setAdapter(mAdapter);

        mWallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        fragmentView = content;

        return content;
    }

    private void cashOutConfirmAmount(BigInteger maximumAmount, float rate) {
        long cents = CurrencyUtil.blockChainValueToCents(maximumAmount);
        Money maximumMoney = Money.create(cents, TG2HM.getDefaultCurrency());

        long resultCents = AlphaFortressness.applyRate(maximumAmount, rate);
        Money resultMoney = Money.create(resultCents, TG2HM.getDefaultCurrency());

        new AlertDialog.Builder(getParentActivity())
                .setTitle("Cash out")
                .setMessage("Maximum amount possible to cash out is " + maximumMoney.toString() + ". You will get " + resultMoney.toString() + ".")
                .setPositiveButton("Cash out", (dialogInterface, i) -> cashOutConfirmBeneficiary(maximumAmount, rate))
                .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                .show();
    }

    private void cashOutConfirmBeneficiary(BigInteger amount, float rate) {
        LoadingUtil.onLoadingStarted();

        AlphaFortressness.getBeneficiaryModel(TG2HM.getDefaultCurrency(), (success, model, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (model != null) {
                Context context = getParentActivity();

                FrameLayout content = new FrameLayout(context);
                content.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), 0);
                ScrollView scroll = new ScrollView(context);
                content.addView(scroll, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 360));

                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.VERTICAL);
                scroll.addView(container, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                for (BeneficiaryModel.Field field: model.fields) {
                    addFieldView(field, container);
                }

                new AlertDialog.Builder(getParentActivity())
                        .setTitle("Bank information")
                        .setView(content)
                        .setPositiveButton("Confirm", (dialogInterface, i) -> {
                            if (model.validate()) {
                                initiateCashOut(amount, rate, model);
                                dialogInterface.dismiss();
                            }
                            else {
                                Toast.makeText(context, "Form not entirely filled", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
            else {
                Toast.makeText(getParentActivity(), "Failed to get beneficiary model", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initiateCashOut(BigInteger amount, float rate, BeneficiaryModel beneficiary) {
        LoadingUtil.onLoadingStarted();

        AlphaFortressness.sell(TG2HM.getDefaultCurrency(), amount, rate, beneficiary, (success, transaction, exception) -> {
            LoadingUtil.onLoadingFinished();

            if (transaction != null) {
                new AlertDialog.Builder(getParentActivity())
                        .setTitle("Cash out in progress")
                        .setMessage("The cash out takes at most 15 minutes.")
                        .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
            else {
                new AlertDialog.Builder(getParentActivity())
                        .setTitle("Cash out failed")
                        .setMessage("There was a problem processing the cash out. Please try again.")
                        .setNeutralButton("OK", (dialogInterface, i) -> dialogInterface.dismiss())
                        .show();
            }
        });
    }

    private void addFieldView(BeneficiaryModel.Field field, ViewGroup parent) {
        TextView title = new TextView(parent.getContext());
        title.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        title.setText(field.title);
        title.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        parent.addView(title, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        EditText input = new EditText(parent.getContext());
        input.setBackground(Theme.createEditTextDrawable(parent.getContext(), false));
        input.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        input.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        input.setHint(field.placeholder);
        input.setText(field.value);

        switch (field.type) {
            case TEXT:
            case EMAIL:
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case NUMBER:
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
        }

        input.setMaxLines(1);
        input.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        input.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {
                field.value = s.toString().trim();
            }

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

        });

        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = AndroidUtilities.dp(4);
        params.bottomMargin = AndroidUtilities.dp(4);
        parent.addView(input, params);
    }

    private void checkBalance() {
        Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

        if (!wallet.isCreated()) {
            mTextBalance.setText(Money.create(0, Currency.USD).toString());
        }
        else {
            mTextBalance.setText("");

            wallet.getBalance((success, usd, eur, errorCause) -> {
                if (success) {
                    mTextBalance.setText(TG2HM.pickTheRightMoney(usd, eur).toString());
                }
                else {
                    mTextBalance.setText("[Connection problem]");
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        checkBalance();

        mAdapter.getData();
    }

    @Override
    protected void clearViews() {
        ((ViewGroup) fragmentView).removeAllViews();

        super.clearViews();

        mTextBalance = null;
        mAdapter = null;
    }

    private class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private List<JSONObject> mTransactions = null;

        public void getData() {
            if (!mWallet.isCreated()) {
                return;
            }

            mWallet.getContractKit((success, contractKit, errorCause) -> {
                if (!success) {
                    // TODO Feedback?
                    return;
                }

                mUSDAddress = contractKit.contracts.getStableToken().getContractAddress();
                mEURAddress = contractKit.contracts.getStableTokenEUR().getContractAddress();

                new Thread() {

                    @Override
                    public void run() {
                        String baseURL = HeymateConfig.MAIN_NET ? "https://explorer.celo.org/" : "https://alfajores-blockscout.celo-testnet.org/";
                        String url = baseURL + "api?module=account&action=tokentx&page=0&offset=50&address=" + mWallet.getAddress();

                        try {
                            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                            connection.setDoOutput(true);
                            InputStream input = connection.getInputStream();
                            String sData = InternalUtils.streamToString(input);
                            connection.disconnect();
                            JSONObject data = new JSONObject(sData);
                            JSONArray jTransactions = data.getJSONArray("result");

                            List<JSONObject> transactions = new ArrayList<>(jTransactions.length());

                            for (int i = 0; i < jTransactions.length(); i++) {
                                BigInteger amount = new BigInteger(jTransactions.getJSONObject(i).getString("value"));
                                long cents = CurrencyUtil.blockChainValueToCents(amount);

                                if (cents > 0) {
                                    transactions.add(jTransactions.getJSONObject(i));
                                }
                            }

                            Utils.postOnUIThread(() -> {
                                mTransactions = transactions;
                                notifyDataSetChanged();
                            });
                        } catch (IOException | JSONException e) {
                            Log.e(TAG, "Failed to get transactions", e);
                        }
                    }

                }.start();
            });
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TransactionItem view = new TransactionItem(parent.getContext());
            view.setCurrencyAddresses(mUSDAddress, mEURAddress);
            return new RecyclerView.ViewHolder(view) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TransactionItem item = (TransactionItem) holder.itemView;

            JSONObject transaction = mTransactions.get(position);
            item.setTransaction(transaction);
        }

        @Override
        public int getItemCount() {
            return mTransactions == null ? 0 : mTransactions.size();
        }

    }

}
