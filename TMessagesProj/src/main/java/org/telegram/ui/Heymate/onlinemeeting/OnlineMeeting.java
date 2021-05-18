package org.telegram.ui.Heymate.onlinemeeting;

import android.content.Context;

import org.telegram.messenger.ApplicationLoader;

public class OnlineMeeting {

    private static OnlineMeeting mInstance = null;

    public static OnlineMeeting get() {
        if (mInstance == null) {
            mInstance = new OnlineMeeting(ApplicationLoader.applicationContext);
        }

        return mInstance;
    }

    private Context mContext;
//    private ZoomSDK mSDK;

    private OnlineMeeting(Context context) {
        mContext = context;

//        ZoomSDKInitParams params = new ZoomSDKInitParams();
//        params.domain = "https://zoom.us"; // Required
//        params.enableLog = true; // Optional for debugging
//
//        mSDK = ZoomSDK.getInstance();
//
//        mSDK.initialize(context, this, params);
//        mSDK.loginWithSSOToken()
    }

}
