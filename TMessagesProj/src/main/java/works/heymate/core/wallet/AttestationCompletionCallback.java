package works.heymate.core.wallet;

import works.heymate.celo.CeloException;

public interface AttestationCompletionCallback {

    void attestationCompletionResult(boolean success, boolean verified, CeloException errorCause);

}
