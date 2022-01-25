package works.heymate.core.wallet;

public interface SignatureCallback {

    void onSignResult(boolean successful, Exception exception);

}
