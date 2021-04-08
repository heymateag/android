package works.heymate.core.wallet;

import works.heymate.celo.CeloException;

public interface BalanceCallback {

    void onBalanceQueryResult(boolean success, long cents, CeloException errorCause);

}
