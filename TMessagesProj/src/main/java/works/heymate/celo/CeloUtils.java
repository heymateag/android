package works.heymate.celo;

import org.celo.contractkit.CeloContract;
import org.celo.contractkit.ContractKit;
import org.celo.contractkit.wrapper.ExchangeWrapper;
import org.celo.contractkit.wrapper.StableTokenWrapper;

import works.heymate.core.Currency;

public class CeloUtils {

    public static void adjustGasPayment(ContractKit contractKit, Currency currency) throws CeloException {
        if (currency == works.heymate.core.Currency.USD) {
            contractKit.setFeeCurrency(CeloContract.StableToken);
        }
        else if (currency == works.heymate.core.Currency.EUR) {
            contractKit.setFeeCurrency(CeloContract.StableTokenEUR);
        }
        else if (currency == Currency.REAL) {
            contractKit.setFeeCurrency(CeloContract.StableTokenBRL);
        }
        else {
            throw new CeloException(CeloError.NETWORK_ERROR, new Exception("Unknown currency: " + currency)); // TODO Unrelated error
        }
    }

    public static StableTokenWrapper getToken(ContractKit contractKit, Currency currency) {
        if (Currency.USD.equals(currency)) {
            return contractKit.contracts.getStableToken();
        }

        if (Currency.EUR.equals(currency)) {
            return contractKit.contracts.getStableTokenEUR();
        }

        if (Currency.REAL.equals(currency)) {
            return contractKit.contracts.getStableTokenBRL();
        }

        return null;
    }

    public static ExchangeWrapper getExchange(ContractKit contractKit, Currency currency) {
        if (Currency.USD.equals(currency)) {
            return contractKit.contracts.getExchange();
        }

        if (Currency.EUR.equals(currency)) {
            return contractKit.contracts.getExchangeEUR();
        }

        if (Currency.REAL.equals(currency)) {
            return contractKit.contracts.getExchangeBRL();
        }

        return null;
    }

}
