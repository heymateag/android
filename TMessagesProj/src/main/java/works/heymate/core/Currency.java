package works.heymate.core;

import androidx.annotation.NonNull;

import works.heymate.ramp.Nimiq;

public class Currency {

    public static final Currency USD = new Currency("USD", "$");
    public static final Currency EUR = new Currency("EUR", "€");

    public static Currency forName(String name) {
        if (USD.name.equals(name)) {
            return USD;
        }

        if (EUR.name.equals(name)) {
            return EUR;
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

        return name.equals(currency.name);
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

        return currency.name;
    }

}
