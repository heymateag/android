package works.heymate.core.wallet;

import org.telegram.ui.Heymate.OfferDto;

import java.util.Hashtable;
import java.util.Map;

import works.heymate.core.HeymateEvents;

public class Wallet {

    private static Map<String, Wallet> mWallets = new Hashtable<>();

    public static Wallet get(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        return mWallets.get(phoneNumber);
    }

    public static Wallet createWallet(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        Wallet wallet = mWallets.get(phoneNumber);

        if (wallet == null) {
            wallet = new Wallet(phoneNumber);

            mWallets.put(phoneNumber, wallet);

            HeymateEvents.notify(HeymateEvents.WALLET_CREATED, phoneNumber, wallet);
        }

        return wallet;
    }

    private String mPhoneNumber;

    private Boolean mVerified = null;
    private boolean mCheckingPhoneNumberVerified = false;

    private Wallet(String phoneNumber) {
        mPhoneNumber = phoneNumber;

        checkPhoneNumberVerified();
    }

    public void checkPhoneNumberVerified() {
        if (mCheckingPhoneNumberVerified) {
            return;
        }

        mCheckingPhoneNumberVerified = true;
        // TODO
    }

    public void initPayment(OfferDto offer) {
        // TODO
    }

}
