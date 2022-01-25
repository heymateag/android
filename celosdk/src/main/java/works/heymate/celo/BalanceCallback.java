package works.heymate.celo;

import java.math.BigInteger;

public interface BalanceCallback {

    void onBalanceResult(boolean success, BigInteger rawCUSD, BigInteger rawCEUR, BigInteger rawCREAL, long cUSDCents, long cEURCents, long cREALCents, CeloException errorCause);

}
