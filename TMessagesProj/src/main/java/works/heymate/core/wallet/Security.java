package works.heymate.core.wallet;

import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.provider.Settings;

import org.telegram.messenger.support.fingerprint.FingerprintManagerCompat;

public class Security {

    public static final int INSECURE = 0;
    public static final int BIOMETRIC = 1;
    public static final int PIN = 2;

    private static final String KEY_SECURITY_MODE = "security_mode";

    public static int getSecurityMode(Wallet wallet) {
        return wallet.getPreferences().getInt(KEY_SECURITY_MODE, INSECURE);
    }

    public static void setSecurityMode(Wallet wallet, int securityMode) {
        wallet.getPreferences().edit().putInt(KEY_SECURITY_MODE, securityMode).apply();;
    }

    public static int getSupportedSecurityModes(Context context) {
        return PIN | (isBiometricSecuritySupported(context) ? BIOMETRIC : 0);
    }

    public static boolean isBiometricSecuritySupported(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            //return ((BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE)).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
            return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_FINGERPRINT); // It's risky.
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ((FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE)).isHardwareDetected();
        }
        else {
            return false;
        }
    }

    public static boolean isSecurityModeAvailable(int mode, Context context) {
        switch (mode) {
            case BIOMETRIC:
                return isBiometricSecurityAvailable(context);
            case PIN:
                return isPinSecurityAvailable(context);
            default:
                return false;
        }
    }

    public static Intent getEnableIntent(int mode) {
        switch (mode) {
            case BIOMETRIC:
                return getBiometricEnrolmentIntent();
            case PIN:
                return getPinSecuritySetupIntent();
            default:
                return new Intent(Settings.ACTION_SECURITY_SETTINGS);
        }
    }

    public static boolean isBiometricSecurityAvailable(Context context) {
        if (!isBiometricSecuritySupported(context)) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return ((BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE)).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ((FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE)).hasEnrolledFingerprints();
        }
        else {
            return false;
        }
    }

    public static Intent getBiometricEnrolmentIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG);
            return intent;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new Intent(Settings.ACTION_FINGERPRINT_ENROLL);
        }
        else {
            return new Intent(Settings.ACTION_SECURITY_SETTINGS);
        }
    }

    public static boolean isPinSecurityAvailable(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return ((BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE)).canAuthenticate(BiometricManager.Authenticators.DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS;
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).isDeviceSecure();
        }
        else {
            return false;
        }
    }

    public static Intent getPinSecuritySetupIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
            intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.DEVICE_CREDENTIAL);
            return intent;
        }
        else {
            return new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
        }
    }

}
