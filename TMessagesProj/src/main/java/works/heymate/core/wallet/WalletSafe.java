package works.heymate.core.wallet;

import android.content.Context;
import android.os.Environment;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESLightEngine;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

import works.heymate.celo.CeloAccount;

public class WalletSafe {

    public static void secureAccount(CeloAccount account, String phoneNumber, Context context) throws IOException {
        AESLightEngine aesLightEngine = getAESEngine(phoneNumber, true);

        JSONObject json = new JSONObject();
        try {
            json.put("private", account.privateKey);
            json.put("public", account.publicKey);
        } catch (JSONException e) { }

        String data = json.toString();

        while (data.length() % 16 != 0) {
            data = data + "X";
        }

        byte[] bytes = data.getBytes();

        int index = 0;

        byte[] buffer = new byte[16];

        OutputStream stream = new FileOutputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "safe"));

        while (index < bytes.length) {
            index += aesLightEngine.processBlock(bytes, index, buffer, 0);

            stream.write(buffer);
        }

        stream.flush();
        stream.close();
    }

    public static CeloAccount restoreAccount(String phoneNumber, Context context) throws IOException {
        AESLightEngine aesLightEngine = getAESEngine(phoneNumber, false);

        InputStream stream = new FileInputStream(new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "safe"));
        byte[] buffer = new byte[16];
        int readSize = 0;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        while (readSize != -1) {
            readSize = stream.read(buffer);

            if (readSize > 0) {
                out.write(buffer, 0, readSize);
            }
        }

        stream.close();

        byte[] encryptedBytes = out.toByteArray();

        out.reset();

        int index = 0;

        while (index < encryptedBytes.length) {
            index += aesLightEngine.processBlock(encryptedBytes, index, buffer, 0);

            out.write(buffer, 0, 16);
        }

        byte[] decryptedBytes = out.toByteArray();

        String data = new String(decryptedBytes);

        int trimIndex = data.indexOf('X');

        if (trimIndex > 0) {
            data = data.substring(0, trimIndex);
        }

        try {
            JSONObject json = new JSONObject(data);

            String privateKey = json.getString("private");
            String publicKey = json.getString("public");

            return new CeloAccount(privateKey, publicKey);
        } catch (JSONException e) {
            throw new IOException("Failed to convert the safe info a json.", e);
        }
    }

    private static AESLightEngine getAESEngine(String phoneNumber, boolean forEncryption) {
        BigInteger bigInteger = new BigInteger(phoneNumber.getBytes());
        long seed = bigInteger.longValue();
        Random random = new Random(seed);

        byte[] key = new byte[16];
        random.nextBytes(key);

        AESLightEngine aesLightEngine = new AESLightEngine();

        KeyParameter keyParameter = new KeyParameter(key);
        aesLightEngine.init(forEncryption, keyParameter);

        return aesLightEngine;
    }

}
