package works.heymate.core.wallet;

import works.heymate.celo.CeloException;
import works.heymate.core.Money;

public interface BalanceCallback {

    void onBalanceQueryResult(boolean success, Money usdBalance, Money eurBalance, CeloException errorCause);

}
