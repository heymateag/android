package works.heymate.core.wallet;

import com.google.gson.GsonBuilder;
import com.trustwallet.walletconnect.WCClient;
import com.trustwallet.walletconnect.models.ethereum.WCEthereumSignMessage;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;
import okhttp3.OkHttpClient;

public class WalletConnection extends WCClient {

    WalletConnection(Wallet wallet) {
        super(new GsonBuilder(), new OkHttpClient.Builder().build());

        setOnSessionRequest((requestId, wcPeerMeta) -> {

            return null;
        });

        setOnGetAccounts(requestId -> {
            return null;
        });

        setOnSignTransaction((aLong, wcSignTransaction) -> {
            return null;
        });

        setOnEthSignTransaction((aLong, wcEthereumTransaction) -> {
            return null;
        });

        setOnEthSign((requestId, wcEthereumSignMessage) -> {

            return null;
        });
    }



}
