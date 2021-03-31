package works.heymate.core.wallet;

public interface SignatureCallback {

    void onSignResult(boolean successful, String signature, Exception exception);

}
