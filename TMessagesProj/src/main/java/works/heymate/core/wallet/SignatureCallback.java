package works.heymate.core.wallet;

public interface SignatureCallback {

    void onSignResult(boolean successful, String priceSignature, String bundleSignature, String subscriptionSignature, Exception exception);

}
