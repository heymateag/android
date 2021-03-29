package works.heymate.core.wallet;

import android.app.Activity;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.biometrics.BiometricManager;
import android.hardware.biometrics.BiometricPrompt;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import org.telegram.messenger.support.fingerprint.FingerprintManagerCompat;

public class Security {

    public static final int INSECURE = 0;
    public static final int BIOMETRIC = 1;
    public static final int PIN = 2;

    private static final String KEY_SECURITY_MODE = "security_mode";

    // This is a bad solution. But maybe it works because keyguard should be light?
    public interface IntentLauncher {

        void startIntentForResult(Intent intent, IntentResultReceiver resultReceiver);

    }

    public interface IntentResultReceiver {

        void onIntentResult(int resultCode);

    }

    public static boolean ensureSecurity(FragmentActivity activity, Wallet wallet, CharSequence title, CharSequence description, IntentLauncher intentLauncher, Runnable task) {
        int securityMode = getSecurityMode(wallet);

        if (securityMode != INSECURE) {
            int supportedSecurityModes = getSupportedSecurityModes(activity);

            if ((securityMode & supportedSecurityModes) != securityMode) {
                return false;
            }

            if (!isSecurityModeAvailable(securityMode, activity)) {
                return true;
            }
        }

        switch (securityMode) {
            case INSECURE:
                task.run();
                break;
            case BIOMETRIC:
                ensureBiometricSecurity(activity, title, description, task);
                break;
            case PIN:
                ensurePinSecurity(activity, title, description, intentLauncher, task);
                break;
        }

        return true;
    }

    private static void ensureBiometricSecurity(FragmentActivity activity, CharSequence title, CharSequence description, Runnable task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(activity)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setTitle(title)
                    .setDescription(description)
                    .build();
            biometricPrompt.authenticate(new CancellationSignal(), ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    task.run();
                }

            });
        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            androidx.biometric.BiometricPrompt.PromptInfo promptInfo = new androidx.biometric.BiometricPrompt.PromptInfo.Builder()
//                    .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG)
//                    .setTitle(title)
//                    .setDescription(description)
//                    .build();
//            androidx.biometric.BiometricPrompt biometricPrompt = new androidx.biometric.BiometricPrompt(activity, ContextCompat.getMainExecutor(activity), new androidx.biometric.BiometricPrompt.AuthenticationCallback() {
//
//                @Override
//                public void onAuthenticationSucceeded(@NonNull androidx.biometric.BiometricPrompt.AuthenticationResult result) {
//                    super.onAuthenticationSucceeded(result);
//                    task.run();
//                }
//
//            });
//            biometricPrompt.authenticate(promptInfo);
            // TODO show dialog
            Toast.makeText(activity, "Put finger on scanner to authenticate", Toast.LENGTH_LONG).show();

            FingerprintManager fingerprintManager = (FingerprintManager) activity.getSystemService(Context.FINGERPRINT_SERVICE);
            fingerprintManager.authenticate(null, null, 0, new FingerprintManager.AuthenticationCallback() {

                @Override
                public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    task.run();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    // TODO Improve
                    Toast.makeText(activity, "Fingerprint authentication failed", Toast.LENGTH_LONG).show();
                }

            }, new Handler(Looper.getMainLooper()));
        }
    }

    private static void ensurePinSecurity(Activity activity, CharSequence title, CharSequence description, IntentLauncher intentLauncher, Runnable task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BiometricPrompt biometricPrompt = new BiometricPrompt.Builder(activity)
                    .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .setTitle(title)
                    .setDescription(description)
                    .build();
            biometricPrompt.authenticate(new CancellationSignal(), ContextCompat.getMainExecutor(activity), new BiometricPrompt.AuthenticationCallback() {

                @Override
                public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    task.run();
                }

            });
        }
//        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);
//            keyguardManager.requestDismissKeyguard(activity, new KeyguardManager.KeyguardDismissCallback() {
//                @Override
//                public void onDismissSucceeded() {
//                    super.onDismissSucceeded();
//                    task.run();
//                }
//            });
//        }
        else {
            KeyguardManager keyguardManager = (KeyguardManager) activity.getSystemService(Context.KEYGUARD_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(title, description);

                if (intent == null) {
                    task.run();
                    return;
                }

                intentLauncher.startIntentForResult(intent, resultCode -> {
                    if (resultCode == Activity.RESULT_OK) {
                        task.run();
                    }
                });
            }
        }
    }

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
            int canAuthenticate = ((BiometricManager) context.getSystemService(Context.BIOMETRIC_SERVICE)).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);
            return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS || canAuthenticate == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
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
