package org.telegram.ui.Heymate.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BackDrawable;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Heymate.HeymateConfig;
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
import works.heymate.core.Texts;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

public class WalletActivity extends BaseFragment {

    private TextView mTextBalance;

    private TransactionAdapter mAdapter;

    private Wallet mWallet;

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

    private void checkBalance() {
        Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

        if (!wallet.isCreated()) {
            mTextBalance.setText("[No wallet detected]");
        }
        else {
            mTextBalance.setText("");

            wallet.getBalance((success, cents, errorCause) -> {
                if (success) {
                    String sDollars = String.valueOf(cents / 100);
                    String sCents = String.valueOf(cents % 100);

                    if (sCents.length() < 2) {
                        sCents = "0" + sCents;
                    }

                    mTextBalance.setText("$" + sDollars + "." + sCents);
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
                        Log.e("WalletActivity", "Failed to get transactions", e);
                    }
                }

            }.start();
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = new TransactionItem(parent.getContext());
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
            return mTransactions == null ? 0 : Math.max(1, mTransactions.size());
        }

    }

}
