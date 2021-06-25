package org.telegram.ui.Heymate.wallet;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.HeymateConfig;
import org.telegram.ui.Heymate.TG2HM;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(context);
        title.setText("Total balance");
        content.addView(title);

        mTextBalance = new TextView(context);
        content.addView(mTextBalance);

        RecyclerView transactionList = new RecyclerView(context);
        transactionList.setLayoutManager(new LinearLayoutManager(context));
        content.addView(transactionList, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1, Gravity.NO_GRAVITY));

        mAdapter = new TransactionAdapter();
        transactionList.setAdapter(mAdapter);

        mWallet = Wallet.get(context, TG2HM.getCurrentPhoneNumber());

        return content;
    }

    private void checkBalance() {
        Wallet wallet = Wallet.get(getParentActivity(), TG2HM.getCurrentPhoneNumber());

        if (!wallet.isCreated()) {
            mTextBalance.setText("Current balance is: [No wallet detected]");
        }
        else {
            mTextBalance.setText("Current balance is:");

            wallet.getBalance((success, cents, errorCause) -> {
                if (success) {
                    mTextBalance.setText("Current balance is: $" + (cents / 100f));
                }
                else {
                    mTextBalance.setText("Current balance is: [Connection problem]");
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
        // TODO
        super.clearViews();
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
                    String url = baseURL + "api?module=account&action=tokentx&page=0&offset=20&address=" + mWallet.getAddress();

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
                            if (!new BigInteger(jTransactions.getJSONObject(i).getString("value")).equals(BigInteger.ZERO)) {
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
            TextView text = new TextView(parent.getContext());
            text.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
            return new RecyclerView.ViewHolder(text) { };
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView text = (TextView) holder.itemView;

            if (mTransactions.size() == 0) {
                text.setText("No transactions");
                return;
            }

            try {
                JSONObject transaction = mTransactions.get(position);

                String from = transaction.getString("from");
                String to = transaction.getString("to");
                long timestamp = Long.parseLong(transaction.getString("timeStamp")) * 1000L;
                BigInteger value = new BigInteger(transaction.getString("value"));

                boolean received = mWallet.getAddress().equals(to);
                long amount = CurrencyUtil.blockChainValueToCents(value);

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm");

                text.setText((received ? "Received: " : "Sent: ") + (amount / 100) + "." + (amount % 100) + " cUSD" + "\nTime: " + simpleDateFormat.format(new Date(timestamp)));
            } catch (Exception e) {
                text.setText("Error! " + e.getMessage());
                Log.e("WalletActivity", "Item load failed", e);
            }
        }

        @Override
        public int getItemCount() {
            return mTransactions == null ? 0 : Math.max(1, mTransactions.size());
        }

    }

}
