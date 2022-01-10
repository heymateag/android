package works.heymate.celo;

import java.math.BigInteger;

public class Amount {

    public static Amount fromBlockchainValue(BigInteger value) {
        return new Amount(value);
    }

    public static Amount fromCents(long cents) {
        return new Amount(CurrencyUtil.centsToBlockChainValue(cents));
    }

    private BigInteger value;

    private Amount(BigInteger value) {
        this.value = value;
    }

    public BigInteger blockchainValue() {
        return value;
    }

    public long cents() {
        return CurrencyUtil.blockChainValueToCents(value);
    }

}
