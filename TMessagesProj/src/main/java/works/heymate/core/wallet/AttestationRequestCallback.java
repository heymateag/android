package works.heymate.core.wallet;

import works.heymate.celo.CeloException;

public interface AttestationRequestCallback {

    void onAttestationRequestResult(Wallet wallet, int newAttestations, boolean requiresMore, CeloException exception);

}
