package works.heymate.core;

import androidx.annotation.NonNull;

import works.heymate.ramp.Nimiq;

public class Currency {

    public static final Currency GOLD = new Currency("GOLD", "Gold");
    public static final Currency USD = new Currency("USD", "$");
    public static final Currency EUR = new Currency("EUR", "€");
    public static final Currency REAL = new Currency("REAL", "R$");
    public static final Currency INR = new Currency("INR", "₹");

    public static final Currency[] CELO_CURRENCIES = { USD, EUR, REAL };
    public static final String[] CURRENCY_NAMES = { USD.name, EUR.name, REAL.name };

    public static Currency forName(String name) {
        if (GOLD.name.equalsIgnoreCase(name)) {
            return GOLD;
        }

        if (USD.name.equalsIgnoreCase(name)) {
            return USD;
        }

        if (EUR.name.equalsIgnoreCase(name)) {
            return EUR;
        }

        if (REAL.name.equalsIgnoreCase(name)) {
            return REAL;
        }

        if (INR.name.equalsIgnoreCase(name)) {
            return INR;
        }

        return new Currency(name, name);
    }

    private final String name;
    private final String symbol;

    private Currency(String name, String symbol) {
        this.name = name;
        this.symbol = symbol;
    }

    public String name() {
        return name;
    }

    public String symbol() {
        return symbol == null ? name : symbol;
    }

    public works.heymate.celo.Currency celoCurrency() {
        if (this.equals(GOLD)) {
            return works.heymate.celo.Currency.GOLD;
        }

        if (this.equals(REAL)) {
            return works.heymate.celo.Currency.REAL;
        }

        if (this.equals(EUR)) {
            return works.heymate.celo.Currency.EUR;
        }

        return works.heymate.celo.Currency.USD;
    }

    public String format(String amount) {
        if (symbol.length() == 1) {
            return symbol + amount;
        }
        else {
            return amount + " " + symbol;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Currency currency = (Currency) o;

        return name.equalsIgnoreCase(currency.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static String getNimiqCurrency(Currency currency) {
        if (USD.equals(currency)) {
            return Nimiq.TOKEN_CUSD;
        }
        else if (EUR.equals(currency)) {
            return Nimiq.TOKEN_CEUR;
        }
        else if (REAL.equals(currency)) {
            return Nimiq.TOKEN_CREAL;
        }

        return currency.name;
    }

}
