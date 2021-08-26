package works.heymate.celo;

import java.math.BigInteger;

public interface BalanceCallback {

    void onBalanceResult(boolean success, BigInteger rawCUSD, BigInteger rawCEUR, long cUSDCents, long cEURCents, CeloException errorCause);

}
