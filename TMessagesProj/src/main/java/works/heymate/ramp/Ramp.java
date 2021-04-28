package works.heymate.ramp;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import works.heymate.core.Texts;

public class Ramp {

    private static final String API_KEY = "nwtv82ts9bpas9k5c5ef8w3mfoy2ak73goq3gvca";

    private static final String RINKEBY_API_KEY = "pskw6bmpx7tomwne2tp3tvrs5c35wexzp7byytdq";

    public static RampDialog getDialog(Context context, String userAddress, String amount) {
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

        return new RampDialog(context, uri.toString());
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
