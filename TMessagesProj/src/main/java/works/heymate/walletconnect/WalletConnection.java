package works.heymate.walletconnect;

import android.app.Activity;

import com.google.gson.GsonBuilder;
import com.trustwallet.walletconnect.WCClient;
import com.trustwallet.walletconnect.models.WCPeerMeta;
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage;
import com.trustwallet.walletconnect.models.ethereum.WCEthereumTransaction;
import com.trustwallet.walletconnect.models.session.WCSession;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.ContractKit;
import org.celo.contractkit.protocol.CeloRawTransaction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.TG2HM;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.OkHttpClient;
import works.heymate.celo.CeloAccount;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

public class WalletConnection {

    private static final String KEY_PEER_ID = "wc_peer_id";
    private static final String KEY_SESSIONS = "wc_sessions";

    private static final String SESSION_SESSION = "session";
    private static final String SESSION_PEER_ID = "peer_id";

    public static WCSession sessionFromUri(String uri) {
        return WCSession.Companion.from(uri);
    }

    private final Wallet mWallet;

    private final String mPeerId;
    private final WCPeerMeta mPeerMeta;

    private final GsonBuilder mGson;
    private final OkHttpClient mOkHttpClient;

    private final Map<String, WCClient> mClients = new HashMap<>();

    public WalletConnection(Wallet wallet) {
        mWallet = wallet;

        String peerId = mWallet.getPreferences().getString(KEY_PEER_ID, null);

        if (peerId == null) {
            peerId = UUID.randomUUID().toString();

            mWallet.getPreferences().edit().putString(KEY_PEER_ID, peerId).apply();
        }

        mPeerId = peerId;

        mPeerMeta = new WCPeerMeta("heymate wallet", "https://works.heymate.beta", "heymate wallet operates on the Celo blockchain only.", Arrays.asList());

        mGson = new GsonBuilder();
        mOkHttpClient = new OkHttpClient.Builder().build();
    }

    public void start() {
        try {
            JSONArray jSessions = new JSONArray(mWallet.getPreferences().getString(KEY_SESSIONS, "[]"));

            for (int i = 0; i < jSessions.length(); i++) {
                JSONObject jSession = jSessions.getJSONObject(i);

                String sSession = jSession.getString(SESSION_SESSION);
                String remotePeerId = jSession.getString(SESSION_PEER_ID);

                WCSession session = sessionFromUri(sSession);

                if (session == null) {
                    continue;
                }

                WCClient client = mClients.get(sSession);

                if (client == null) {
                    client = newClient(sSession);

                    mClients.put(sSession, client);
                }

                if (!client.isConnected()) {
                    client.connect(session, mPeerMeta, mPeerId, remotePeerId);
                }
            }
        } catch (JSONException e) { }
    }

    public void connect(WCSession session) {
        WCClient client = mClients.get(session.toUri());

        if (client != null) {
            if (!client.isConnected()) {
                client.connect(session, mPeerMeta, mPeerId, null);
            }

            return;
        }

        new Thread() {

            @Override
            public void run() {
                newClient(session.toUri()).connect(session, mPeerMeta, mPeerId, null);
            }

        }.start();
    }

    private WCClient newClient(String sSession) {
        WCClient client = new WCClient(mGson, mOkHttpClient);

        client.setOnSessionRequest((requestId, wcPeerMeta) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    client.rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Connection request")
                        .setMessage(wcPeerMeta.component1() + " wants to connect to your heymate wallet.")
                        .setPositiveButton("Accept", (dialogInterface, i) -> {
                            client.approveSession(Arrays.asList(mWallet.getAddress()), Wallet.CELO_CONTEXT.chainId);

                            mClients.put(sSession, client);

                            addSessionToSessions(sSession, client.getRemotePeerId());
                        })
                        .setNegativeButton("Reject", (dialogInterface, i) -> client.rejectSession("Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> client.rejectSession("Session request rejected by user."))
                        .show();
            });

            return null;
        });

        client.setOnGetAccounts(requestId -> {
            client.approveRequest(requestId, Arrays.asList(mWallet.getAddress()));
            return null;
        });

        // personal_sign
        // eth_sign
        // eth_signTypedData
        client.setOnEthSign((requestId, wcEthereumSignMessage) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    client.rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Sign a message")
                        .setMessage("Sign a message for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> processRequest(client, requestId, wcEthereumSignMessage))
                        .setNegativeButton("Reject", (dialogInterface, i) -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });

            return null;
        });

        // eth_signTransaction
        client.setOnEthSignTransaction((requestId, wcEthereumTransaction) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    client.rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Sign a transaction")
                        .setMessage("Sign a transaction for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> mWallet.getContractKit((success, contractKit, errorCause) -> {
                            if (success) {
                                try {
                                    CeloRawTransaction transaction = wcTransactionToCeloTransaction(contractKit, wcEthereumTransaction);
                                    String signature = contractKit.transactionManager.sign(transaction);
                                    client.approveRequest(requestId, signature);
                                } catch (IOException e) {
                                    client.rejectRequest(requestId, "Failed to sign the transaction: " + e.getMessage());
                                }
                            }
                            else {
                                client.rejectRequest(requestId, "Blockchain network error.");
                            }
                        }))
                        .setNegativeButton("Reject", (dialogInterface, i) -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });
            return null;
        });

        client.setOnEthSendTransaction((requestId, wcEthereumTransaction) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    client.rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Send a transaction")
                        .setMessage("Send a transaction for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> mWallet.getContractKit((success, contractKit, errorCause) -> {
                            if (success) {
                                try {
                                    CeloRawTransaction transaction = wcTransactionToCeloTransaction(contractKit, wcEthereumTransaction);

                                    EthSendTransaction result = contractKit.transactionManager.signAndSend(transaction);

                                    if (result.hasError()) {
                                        client.rejectRequest(requestId, result.getError().getMessage());
                                    }
                                    else {
                                        client.approveRequest(requestId, result.getTransactionHash());
                                    }
                                } catch (IOException e) {
                                    client.rejectRequest(requestId, "Failed to send the transaction: " + e.getMessage());
                                }
                            }
                            else {
                                client.rejectRequest(requestId, "Blockchain network error.");
                            }
                        }))
                        .setNegativeButton("Reject", (dialogInterface, i) -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> client.rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });
            return null;
        });

        return client;
    }

    private void processRequest(WCClient client, long requestId, WCEthereumSignMessage request) {
        switch (request.getType()) {
            case MESSAGE:
            case PERSONAL_MESSAGE:
                client.approveRequest(requestId, sign(request.getData()));
                return;
            case TYPED_MESSAGE:
                // TODO
                return;
        }
    }

    private String sign(String data) {
        CeloAccount account = mWallet.getAccount();
        ECKeyPair keyPair = new ECKeyPair(Numeric.toBigInt(account.privateKey), Numeric.toBigInt(account.publicKey));

        Sign.SignatureData signatureData = Sign.signPrefixedMessage(Numeric.hexStringToByteArray(data), keyPair);

        return Numeric.toHexString(signatureData.getR()) + Numeric.toHexStringNoPrefix(signatureData.getS()) + Numeric.toHexStringNoPrefix(signatureData.getV());
    }

    private CeloRawTransaction wcTransactionToCeloTransaction(ContractKit contractKit, WCEthereumTransaction wcEthereumTransaction) throws IOException {
        return new CeloRawTransaction(
                wcEthereumTransaction.getNonce() == null ? getNonce(contractKit) : Numeric.toBigInt(wcEthereumTransaction.getNonce()),
                //wcEthereumTransaction.getGasPrice() == null ? DefaultGasProvider.GAS_PRICE : Numeric.toBigInt(wcEthereumTransaction.getGasPrice()),
                DefaultGasProvider.GAS_PRICE,
                wcEthereumTransaction.getGasLimit() == null ? DefaultGasProvider.GAS_LIMIT : Numeric.toBigInt(wcEthereumTransaction.getGasLimit()),
                wcEthereumTransaction.getTo(),
                wcEthereumTransaction.getValue() == null ? null : Numeric.toBigInt(wcEthereumTransaction.getValue()),
                wcEthereumTransaction.getData(),
                getGasCurrency(contractKit),
                null,
                null
        );
    }

    private BigInteger getNonce(ContractKit contractKit) throws IOException {
        EthGetTransactionCount ethGetTransactionCount = (EthGetTransactionCount)contractKit.web3j.ethGetTransactionCount(contractKit.getAddress(), DefaultBlockParameterName.PENDING).send();
        return ethGetTransactionCount.getTransactionCount();
    }

    private String getGasCurrency(ContractKit contractKit) {
        Currency currency = TG2HM.getDefaultCurrency();

        if (currency == Currency.USD) {
            return contractKit.contracts.addressFor(CeloContract.StableToken);
        }
        else {
            return contractKit.contracts.addressFor(CeloContract.StableTokenEUR);
        }
    }

    private void addSessionToSessions(String sSession, String remotePeerId) {
        JSONObject jSession = new JSONObject();
        try {
            jSession.put(SESSION_SESSION, sSession);
            jSession.put(SESSION_PEER_ID, remotePeerId);
        } catch (JSONException e) { }

        try {
            JSONArray jSessions = new JSONArray(mWallet.getPreferences().getString(KEY_SESSIONS, "[]"));
            jSessions.put(jSession);

            mWallet.getPreferences().edit().putString(KEY_SESSIONS, jSessions.toString()).apply();
        } catch (JSONException e) { }
    }

}
