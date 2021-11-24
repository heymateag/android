package works.heymate.walletconnect;

import android.app.Activity;

import com.google.gson.GsonBuilder;
import com.trustwallet.walletconnect.WCClient;
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage;

import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.OkHttpClient;
import works.heymate.celo.CeloAccount;
import works.heymate.core.Utils;
import works.heymate.core.wallet.Wallet;

public class WalletConnection extends WCClient {

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

        // eth_signTransaction
        setOnEthSignTransaction((requestId, wcEthereumTransaction) -> {
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
                        .setPositiveButton("Accept", (dialogInterface, i) -> approveRequest(requestId, sign(wcEthereumSignMessage.getData())))
                        .setNegativeButton("Reject", (dialogInterface, i) -> rejectRequest(requestId, "Session request rejected by user."))
                        .setOnCancelListener(dialogInterface -> rejectRequest(requestId, "Session request rejected by user."))
                        .show();
            });

            return null;
        });
    }

    private String sign(String data) {
        CeloAccount account = mWallet.getAccount();
        ECKeyPair keyPair = new ECKeyPair(Numeric.toBigInt(account.privateKey), Numeric.toBigInt(account.publicKey));

        Sign.SignatureData signatureData = Sign.signPrefixedMessage(Numeric.hexStringToByteArray(data), keyPair);

        return Numeric.toHexString(signatureData.getR()) + Numeric.toHexStringNoPrefix(signatureData.getS()) + Numeric.toHexStringNoPrefix(signatureData.getV());
    }

}
