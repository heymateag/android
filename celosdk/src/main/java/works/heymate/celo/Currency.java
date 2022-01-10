package works.heymate.celo;

import org.celo.contractkit.ContractKit;
import org.celo.contractkit.wrapper.ExchangeWrapper;
import org.celo.contractkit.wrapper.ICeloTokenWrapper;

public enum Currency {

    GOLD, USD, EUR, REAL;

    public ICeloTokenWrapper getToken(ContractKit contractKit) {
        switch (this) {
            case GOLD:
                return contractKit.contracts.getGoldToken();
            case USD:
                return contractKit.contracts.getStableToken();
            case EUR:
                return contractKit.contracts.getStableTokenEUR();
            case REAL:
                return contractKit.contracts.getStableTokenBRL();
        }

        throw new IllegalStateException("Unknown currency");
    }

    public ExchangeWrapper getExchange(ContractKit contractKit) {
        switch (this) {
            case GOLD:
                throw new IllegalStateException("Gold does not have exchange");
            case USD:
                return contractKit.contracts.getExchange();
            case EUR:
                return contractKit.contracts.getExchangeEUR();
            case REAL:
                return contractKit.contracts.getExchangeBRL();
        }

        throw new IllegalStateException("Unknown currency");
    }

}
