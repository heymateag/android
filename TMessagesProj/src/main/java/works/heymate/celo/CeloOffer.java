package works.heymate.celo;

import org.celo.contractkit.ContractKit;
import org.celo.contractkit.protocol.CeloRawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;

import java.math.BigInteger;
import java.security.SecureRandom;

import works.heymate.celo.contract.Offer;

public class CeloOffer {

    private Offer mContract;

    public CeloOffer(String address, ContractKit contractKit) {
        mContract = Offer.load(address, contractKit.web3j, contractKit.transactionManager, new DefaultGasProvider());
    }

    /*
    Offer needs:

    What is the format of rate? DAAAAAAH

    initialDeposit: long
    firstCancellationMinutes: integer
    firstCancellationPercent: float (0-100)
    secondCancellationMinutes: integer
    secondCancellationPercent: float (0-100)
    delayMinutes: integer
    delayPercentage: float (0-100)

    serviceProviderAddress: string
    serviceProviderSignature: string
     */
    public void create(org.telegram.ui.Heymate.AmplifyModels.Offer offer, long startTime) {
        byte[] tradeId = new byte[16];
        new SecureRandom().nextBytes(tradeId);

        long amount = (long) (Double.parseDouble(offer.getRate()) * 100);

//        mContract.createOffer(
//                tradeId,
//                BigInteger.valueOf(amount),
//                BigInteger.ONE, // fee
//                BigInteger.valueOf(offer.getExpiry().toDate().getTime()),
//                BigInteger.valueOf(startTime),
//                initialDeposit, userAddresses, IntsConfig, signature, BigInteger.ZERO).send();
    }

}
