package works.heymate.celo;

import org.web3j.crypto.Bip32ECKeyPair;
import org.web3j.crypto.Bip39Wallet;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.MnemonicUtils;
import org.web3j.utils.Numeric;

import java.security.SecureRandom;

public class CeloAccount {

    /**
     * This method is time consuming. You might wanna consider to run it in a separate thread.
     * @return
     */
    public static CeloAccount randomAccount() {
        byte[] privateKeyBytes = new byte[16];
        new SecureRandom().nextBytes(privateKeyBytes);

        ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);

        String privateKey = keyPair.getPrivateKey().toString(16);
        String publicKey = keyPair.getPublicKey().toString(16);

        return new CeloAccount(privateKey, publicKey);
    }

    /**
     * This method is time consuming. You might wanna consider to run it in a separate thread.
     * @param mnemonic
     * @return
     */
    public static CeloAccount fromMnemonic(String mnemonic) {
        try {
            byte[] privateKeyBytes = MnemonicUtils.generateEntropy(mnemonic);

            ECKeyPair keyPair = ECKeyPair.create(privateKeyBytes);

            String privateKey = keyPair.getPrivateKey().toString(16);
            String publicKey = keyPair.getPublicKey().toString(16);

            return new CeloAccount(privateKey, publicKey);
        } catch (Throwable t) {
            return null;
        }
    }

    public final String privateKey;
    public final String publicKey;

    public CeloAccount(String privateKey, String publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    public String getMnemonic() {
//        byte[] entropy = new byte[32];
//        new SecureRandom().nextBytes(entropy);
//        String mnemonic = MnemonicUtils.generateMnemonic(entropy);
//        byte[] seed = MnemonicUtils.generateSeed(mnemonic, null);
//        String CELO_DERIVATION_PATH_BASE = "m/44'/52752'/0'";
        // https://github.com/bitcoinjs/bip39/blob/master/src/index.js
        // https://github.com/celo-org/celo-monorepo/blob/master/packages/sdk/utils/package.json
//        Bip32ECKeyPair..generateKeyPair(seed);
        // https://github.com/celo-org/celo-monorepo/blob/master/packages/sdk/utils/src/account.ts

        return MnemonicUtils.generateMnemonic(Numeric.hexStringToByteArray(privateKey));
    }

}
