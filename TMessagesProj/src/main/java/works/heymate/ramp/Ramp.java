package works.heymate.ramp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import org.jetbrains.annotations.NotNull;
import org.telegram.ui.Heymate.ActivityMonitor;
import org.telegram.ui.Heymate.HeymateConfig;

import network.ramp.sdk.events.model.Purchase;
import network.ramp.sdk.facade.Config;
import network.ramp.sdk.facade.RampCallback;
import network.ramp.sdk.facade.RampSDK;
import works.heymate.celo.CurrencyUtil;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Texts;

public class Ramp {

    private static final String PRODUCTION_URL = "https://buy.ramp.network";
    private static final String STAGING_URL = "https://ri-widget-staging.firebaseapp.com/";
    private static final String URL = HeymateConfig.MAIN_NET ? PRODUCTION_URL : STAGING_URL;

    private static final String MAIN_API_KEY = "nwtv82ts9bpas9k5c5ef8w3mfoy2ak73goq3gvca";
    private static final String RINKEBY_API_KEY = "pskw6bmpx7tomwne2tp3tvrs5c35wexzp7byytdq";
    private static final String API_KEY = HeymateConfig.MAIN_NET ? MAIN_API_KEY : RINKEBY_API_KEY;

    public interface RampNetworkCallback {

        void onRampDone(boolean successful);

    }

    public static void init(String userAddress, Money money, RampNetworkCallback callback) {
        RampSDK rampSDK = new RampSDK();

        String currency;

        if (money.getCurrency().equals(Currency.USD)) {
            currency = "CUSD";
        }
        else if (money.getCurrency().equals(Currency.EUR)) {
            currency = "CEUR";
        }
        else if (money.getCurrency().equals(Currency.REAL)) {
            currency = "CREAL"; // TODO Really?
        }
        else {
            callback.onRampDone(false);
            return;
        }

        Config config = new Config(
                Texts.get(Texts.HEYMATE).toString(),
                Texts.get(Texts.LOGO_URL).toString(),
                URL,
                currency,
                CurrencyUtil.centsToBlockChainValue(money.getCents()).toString(),
                "",
                "",
                userAddress,
                "",
                "",
                "",
                "",
                API_KEY
        );

        rampSDK.startTransaction(ActivityMonitor.get().getCurrentActivity(), config, new RampCallback() {

            @Override
            public void onPurchaseFailed() {
                callback.onRampDone(false);
            }

            @Override
            public void onPurchaseCreated(@NotNull Purchase purchase, @NotNull String purchaseViewToken, @NotNull String apiUrl) {
                callback.onRampDone(true);
            }

            @Override
            public void onWidgetClose() {
                // Nothing to do: Called when Ramp finishes the flow and can be closed, or user closed it manually.
            }

        });
    }

    public static RampDialog getDialog(Context context, String userAddress, String amount, RampDialog.OnRampDoneListener listener) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("buy.ramp.network")
                .appendQueryParameter("swapAsset", "CUSD")
                .appendQueryParameter("userAddress", userAddress)
                .appendQueryParameter("hostApiKey", API_KEY)
                .appendQueryParameter("hostAppName", Texts.get(Texts.HEYMATE).toString())
                .appendQueryParameter("hostLogoUrl", Texts.get(Texts.LOGO_URL).toString())
                .appendQueryParameter("swapAmount", amount)
                .build();

        return new RampDialog(context, uri.toString(), listener);
    }

    public static Intent getTopUpIntent(String userAddress, String amount, String returnUrl) {
        Uri uri = new Uri.Builder()
                .scheme("https")
                .authority("buy.ramp.network")
                .appendQueryParameter("swapAsset", "CUSD")
                .appendQueryParameter("userAddress", userAddress)
                .appendQueryParameter("hostApiKey", API_KEY)
                .appendQueryParameter("hostAppName", Texts.get(Texts.HEYMATE).toString())
                .appendQueryParameter("hostLogoUrl", Texts.get(Texts.LOGO_URL).toString())
                .appendQueryParameter("swapAmount", amount)
                .appendQueryParameter("finalUrl", returnUrl)
                .build();

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);

        return intent;
    }

}
