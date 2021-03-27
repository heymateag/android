package works.heymate.core;

import android.os.Handler;
import android.os.Looper;

import org.telegram.ui.Heymate.AmplifyModels.Offer;

public class Utils {

    public static final String HOST = "heymate.works";
    public static final String PATH_OFFER = "offer";

    private static Handler mHandler = null;

    public static String deepLinkForOffer(Offer offer) {
        StringBuilder sb = new StringBuilder();

        sb
                .append("https://").append(HOST).append('/')
                .append(PATH_OFFER).append('/')
                .append(offer.getId()).append("?data=");

        offer.toString();

        return sb.toString();
    }

    public static void runOnUIThread(Runnable runnable) {
        if (Thread.currentThread() == Looper.getMainLooper().getThread()) {
            runnable.run();
            return;
        }

        ensureHandler();

        mHandler.post(runnable);
    }

    public static void postOnUIThread(Runnable runnable) {
        ensureHandler();

        mHandler.post(runnable);
    }

    synchronized private static void ensureHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
    }

}
