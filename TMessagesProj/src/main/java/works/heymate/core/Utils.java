package works.heymate.core;

import android.os.Handler;
import android.os.Looper;

public class Utils {

    private static Handler mHandler = null;

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
