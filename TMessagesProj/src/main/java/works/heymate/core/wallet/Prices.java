package works.heymate.core.wallet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import works.heymate.celo.CurrencyUtil;
import works.heymate.core.Currency;
import works.heymate.core.Money;
import works.heymate.core.Utils;

public class Prices {

    public interface PriceCallback {

        void onPriceReady(Money money);

    }

    private static final Map<Currency, Map<Currency, Map<Long, BigInteger>>> cache = new HashMap<>();
    private static final Map<Money, Map<Currency, List<PriceCallback>>> pendingLookups = new HashMap<>();

    public static void get(Wallet wallet, Money money, Currency targetCurrency, PriceCallback callback) {
        if (money.getCurrency().equals(targetCurrency)) {
            Utils.postOnUIThread(() -> callback.onPriceReady(money));
            return;
        }

        Map<Currency, Map<Long, BigInteger>> targetCache = cache.get(money.getCurrency());
        Map<Long, BigInteger> priceMap;

        if (targetCache != null) {
            priceMap = targetCache.get(targetCurrency);

            if (priceMap != null) {
                BigInteger price = priceMap.get(money.getCents());

                if (price != null) {
                    Utils.postOnUIThread(() -> callback.onPriceReady(Money.create(CurrencyUtil.blockChainValueToCents(price) + 1, targetCurrency)));
                    return;
                }
            }
            else {
                priceMap = new HashMap<>();
                targetCache.put(targetCurrency, priceMap);
            }
        }
        else {
            targetCache = new HashMap<>();
            cache.put(money.getCurrency(), targetCache);

            priceMap = new HashMap<>();
            targetCache.put(targetCurrency, priceMap);
        }

        Map<Currency, List<PriceCallback>> targetCallbacks = pendingLookups.get(money);
        List<PriceCallback> callbacks;

        if (targetCallbacks != null) {
            callbacks = targetCallbacks.get(targetCurrency);

            if (callbacks != null && !callbacks.isEmpty()) {
                callbacks.add(callback);
                return;
            }

            if (callbacks == null) {
                callbacks = new LinkedList<>();
                targetCallbacks.put(targetCurrency, callbacks);
            }
        }
        else {
            targetCallbacks = new HashMap<>();
            pendingLookups.put(money, targetCallbacks);

            callbacks = new LinkedList<>();
            targetCallbacks.put(targetCurrency, callbacks);
        }

        callbacks.add(callback);

        wallet.calculatePrice(money, targetCurrency, (success, amount, errorCause) -> {
            List<PriceCallback> callList = pendingLookups.get(money).remove(targetCurrency);

            Money result;

            if (success) {
                result = Money.create(amount.cents() + 1, targetCurrency);

                cache.get(money.getCurrency()).get(targetCurrency).put(money.getCents(), amount.blockchainValue());
            }
            else {
                result = money;
            }

            for (PriceCallback c: callList) {
                c.onPriceReady(result);
            }
        });
    }

}
