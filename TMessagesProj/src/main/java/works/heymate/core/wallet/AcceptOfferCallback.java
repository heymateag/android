package works.heymate.core.wallet;

import works.heymate.celo.CeloException;

public interface AcceptOfferCallback {

    void onAcceptOfferResult(boolean success, CeloException errorCause);

}
