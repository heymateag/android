package works.heymate.core.wallet;

import works.heymate.celo.CeloException;

public interface OfferOperationCallback {

    void onOfferOperationResult(boolean success, CeloException errorCause);

}
