package works.heymate.celo;

import java.math.BigInteger;

public class Money {

    public static Money get(Amount amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money get(long cents, Currency currency) {
        return get(Amount.fromCents(cents), currency);
    }

    public static Money get(BigInteger value, Currency currency) {
        return get(Amount.fromBlockchainValue(value), currency);
    }

    public final Amount amount;
    public final Currency currency;

    private Money(Amount amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }

}
