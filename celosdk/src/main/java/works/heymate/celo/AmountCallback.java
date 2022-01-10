package works.heymate.celo;

public interface AmountCallback {

    void onAmountResult(boolean success, Amount amount, CeloException errorCause);

}
