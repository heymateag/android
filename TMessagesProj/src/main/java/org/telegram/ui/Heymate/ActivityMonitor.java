package org.telegram.ui.Heymate;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;

public class ActivityMonitor implements Application.ActivityLifecycleCallbacks {

    private static ActivityMonitor mInstance = null;

    public static ActivityMonitor get() {
        if (mInstance == null) {
            mInstance = new ActivityMonitor();
        }

        return mInstance;
    }

    private final LinkedList<Activity> mActivityStack = new LinkedList<>();

    private ActivityMonitor() {

    }

    public Activity getCurrentActivity() {
        return mActivityStack.isEmpty() ? null : mActivityStack.getLast();
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        mActivityStack.add(activity);
    }

    @Override public void onActivityStarted(@NonNull Activity activity) { }
    @Override public void onActivityResumed(@NonNull Activity activity) { }
    @Override public void onActivityPaused(@NonNull Activity activity) { }
    @Override public void onActivityStopped(@NonNull Activity activity) { }
    @Override public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) { }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        mActivityStack.remove(activity);
    }

}
