package works.heymate.core.wallet;

import org.telegram.ui.Heymate.OfferDto;

import java.util.Hashtable;
import java.util.Map;

public class Wallet {

    private static Map<String, Wallet> mWallets = new Hashtable<>();

    public static Wallet get(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        Wallet wallet = mWallets.get(phoneNumber);

        if (wallet == null) {
            wallet = new Wallet(phoneNumber);

            mWallets.put(phoneNumber, wallet);
        }

        return wallet;
    }

    private String mPhoneNumber;

    private Wallet(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public void initPayment(OfferDto offer) {
        // TODO
    }

}
