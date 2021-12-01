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
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.TG2HM;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import okhttp3.OkHttpClient;
import works.heymate.celo.CeloAccount;
import works.heymate.core.Currency;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

public class WalletConnection extends WCClient {

    public static WCSession sessionFromUri(String uri) {
        return WCSession.Companion.from(uri);
    }

    private final Wallet mWallet;

    public WalletConnection(Wallet wallet) {
        super(new GsonBuilder(), new OkHttpClient.Builder().build());

        mWallet = wallet;

        setOnSessionRequest((requestId, wcPeerMeta) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Connection request")
                        .setMessage(wcPeerMeta.component1() + " wants to connect to your heymate wallet.")
                        .setPositiveButton("Accept", (dialogInterface, i) -> approveSession(Arrays.asList(wallet.getAddress()), Wallet.CELO_CONTEXT.chainId))
                        .setNegativeButton("Reject", (dialogInterface, i) -> rejectSession("Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> rejectSession("Session request rejected by user."))
                        .show();
            });

            return null;
        });

        setOnGetAccounts(requestId -> {
            approveRequest(requestId, Arrays.asList(wallet.getAddress()));
            return null;
        });

        // personal_sign
        // eth_sign
        // eth_signTypedData
        setOnEthSign((requestId, wcEthereumSignMessage) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Sign a message")
                        .setMessage("Sign a message for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> processRequest(requestId, wcEthereumSignMessage))
                        .setNegativeButton("Reject", (dialogInterface, i) -> rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });

            return null;
        });

        // eth_signTransaction
        setOnEthSignTransaction((requestId, wcEthereumTransaction) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Sign a transaction")
                        .setMessage("Sign a transaction for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> mWallet.getContractKit((success, contractKit, errorCause) -> {
                            if (success) {
                                CeloRawTransaction transaction = wcTransactionToCeloTransaction(contractKit, wcEthereumTransaction);
                                String signature = contractKit.transactionManager.sign(transaction);
                                approveRequest(requestId, signature);
                            }
                            else {
                                rejectRequest(requestId, "Blockchain network error.");
                            }
                        }))
                        .setNegativeButton("Reject", (dialogInterface, i) -> rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });
            return null;
        });

        setOnEthSendTransaction((requestId, wcEthereumTransaction) -> {
            Utils.postOnUIThread(() -> {
                Activity activity = ActivityMonitor.get().getCurrentActivity();

                if (activity == null) {
                    rejectSession("App is not open");
                    return;
                }

                new AlertDialog.Builder(activity)
                        .setTitle("Sign a transaction")
                        .setMessage("Send a transaction for the connected DAPP?")
                        .setPositiveButton("Accept", (dialogInterface, i) -> mWallet.getContractKit((success, contractKit, errorCause) -> {
                            if (success) {
                                CeloRawTransaction transaction = wcTransactionToCeloTransaction(contractKit, wcEthereumTransaction);

                                try {
                                    EthSendTransaction result = contractKit.transactionManager.signAndSend(transaction);

                                    if (result.hasError()) {
                                        rejectRequest(requestId, result.getError().getMessage());
                                    }
                                    else {
                                        approveRequest(requestId, result.getTransactionHash());
                                    }
                                } catch (IOException e) {
                                    rejectRequest(requestId, "Failed to send the transaction: " + e.getMessage());
                                }
                            }
                            else {
                                rejectRequest(requestId, "Blockchain network error.");
                            }
                        }))
                        .setNegativeButton("Reject", (dialogInterface, i) -> rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });
            return null;
        });
    }

    private void processRequest(long requestId, WCEthereumSignMessage request) {
        switch (request.getType()) {
            case MESSAGE:
            case PERSONAL_MESSAGE:
                approveRequest(requestId, sign(request.getData()));
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

    private CeloRawTransaction wcTransactionToCeloTransaction(ContractKit contractKit, WCEthereumTransaction wcEthereumTransaction) {
        return new CeloRawTransaction(
                Numeric.toBigInt(wcEthereumTransaction.getNonce()),
                Numeric.toBigInt(wcEthereumTransaction.getGasPrice()),
                Numeric.toBigInt(wcEthereumTransaction.getGasLimit()),
                wcEthereumTransaction.getTo(),
                Numeric.toBigInt(wcEthereumTransaction.getValue()),
                wcEthereumTransaction.getData(),
                getGasCurrency(contractKit),
                null,
                null
        );
    }

    public void connect(WCSession session) {
        new Thread() {

            @Override
            public void run() {
                WCPeerMeta peerMeta = new WCPeerMeta("heymate wallet", "https://works.heymate.beta", "heymate wallet operates on the Celo blockchain only.", Arrays.asList());
                connect(session, peerMeta, UUID.randomUUID().toString(), null);
            }

        }.start();
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

}
