package org.celo;

import java.util.Random;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

import android.util.Log;
import android.util.Base64;

/**
 * Taken from https://github.com/celo-org/react-native-blind-threshold-bls
 */
public class BlindThresholdBlsModule {

    static {
        Native.register(BlindThresholdBlsModule.class, "blind_threshold_bls");
    }

    private static final String TAG = "BlindThresholdBlsModule";

    private PointerByReference blindingFactor;
    private Buffer messageBuf;

    public BlindThresholdBlsModule() {

    }

    public String blindMessage(String message) throws Exception {
        try {
            Log.d(TAG, "Preparing blind message buffers");
            byte[] messageBytes = Base64.decode(message, Base64.DEFAULT);
            messageBuf = new Buffer(messageBytes);
            Buffer blindedMessageBuf = new Buffer();

            Log.d(TAG, "Preparing blinding seed");
            Random random = new Random();
            byte[] seed = new byte[32];
            random.nextBytes(seed);
            Buffer seedBuf = new Buffer(seed);

            Log.d(TAG, "Calling blind");
            blindingFactor = new PointerByReference();
            blind(messageBuf, seedBuf, blindedMessageBuf, blindingFactor);

            Log.d(TAG, "Blind call done, retrieving blinded message from buffer");
            byte[] blindedMessageBytes = blindedMessageBuf.getMessage();
            String b64BlindedMessage = Base64.encodeToString(blindedMessageBytes, Base64.DEFAULT);

            Log.d(TAG, "Cleaning up memory");
            free_vector(blindedMessageBuf.message, blindedMessageBuf.len);

            return b64BlindedMessage;
        } catch (Exception e) {
            Log.e(TAG, "Exception while blinding the message: " + e.getMessage());
            throw e;
        }
    }

    public String unblindMessage(String base64BlindedSignature, String base64SignerPublicKey) throws Exception {
        try {
            Log.d(TAG, "Preparing unblind buffers");
            byte[] blindedSigBytes = Base64.decode(base64BlindedSignature, Base64.DEFAULT);
            Buffer blindedSigBuf = new Buffer(blindedSigBytes);
            Buffer unblindedSigBuf = new Buffer();

            Log.d(TAG, "Calling unblind");
            unblind(blindedSigBuf, blindingFactor.getValue(), unblindedSigBuf);

            Log.d(TAG, "Unblind call resul done. Deserializing public key");
            PointerByReference publicKey = new PointerByReference();
            byte[] signerPublicKeyBytes = Base64.decode(base64SignerPublicKey, Base64.DEFAULT);
            deserialize_pubkey(signerPublicKeyBytes, publicKey);

            Log.d(TAG, "Verifying the signatures");
            boolean signatureValid = verify(publicKey.getValue(), messageBuf, unblindedSigBuf);

            if (signatureValid == true) {
                Log.d(TAG, "Verify call done, retrieving signed message from buffer");
                byte[] unblindedSigBytes = unblindedSigBuf.getMessage();
                String b64UnblindedSig = Base64.encodeToString(unblindedSigBytes, Base64.DEFAULT);

                cleanUpMemory(unblindedSigBuf, publicKey);

                return b64UnblindedSig;
            } else {
                Log.d(TAG, "Invalid threshold signature found when verifying");

                cleanUpMemory(unblindedSigBuf, publicKey);

                throw new Exception("Invalid threshold signature");
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception while unblinding the signature: " + e.getMessage());
            throw e;
        }
    }

    private void cleanUpMemory(Buffer unblindedSigBuf, PointerByReference publicKey) {
        Log.d(TAG, "Cleaning up memory");
        messageBuf = null;
        destroy_token(blindingFactor.getValue());
        blindingFactor = null;
        free_vector(unblindedSigBuf.message, unblindedSigBuf.len);
        destroy_pubkey(publicKey.getValue());
    }

    // TODO implement a cleanup method that destroys token if user cancels btwn blinding and unblinding

    // These native methods map to the FFI bindings defined here: 
    // https://github.com/celo-org/celo-threshold-bls-rs/blob/master/ffi/threshold.h
    // Note, seed must be >= 32 characters long
    private static native void blind(Buffer message, Buffer seed, Buffer blinded_message_out, PointerByReference blinding_factor_out);
    private static native boolean unblind(Buffer blinded_signature, Pointer blinding_factor, Buffer unblinded_signature);
    private static native boolean deserialize_pubkey(byte[] pubkey_buf, PointerByReference pubkey);
    private static native boolean verify(Pointer public_key, Buffer message, Buffer signature);
    private static native void free_vector(Pointer bytes, int len);
    private static native void destroy_token(Pointer token);
    private static native void destroy_pubkey(Pointer public_key);
}