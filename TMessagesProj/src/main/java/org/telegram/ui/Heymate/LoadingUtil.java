package org.telegram.ui.Heymate;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.ActionBar.AlertDialog;

import works.heymate.core.Utils;

public class LoadingUtil {

    private static AlertDialog mLoadingDialog = null;
    private static int mLoadingCount = 0;

    public static void onLoadingStarted() {
        Utils.runOnUIThread(() -> {
            mLoadingCount++;

            if (mLoadingDialog == null) {
                mLoadingDialog = new AlertDialog(ApplicationLoader.applicationContext, 3);
                mLoadingDialog.setCanCacnel(false);
            }

            mLoadingDialog.show();
        });
    }

    public static void onLoadingFinished() {
        Utils.runOnUIThread(() -> {
            mLoadingCount--;

            if (mLoadingDialog == null) {
                return;
            }

            if (mLoadingCount <= 0) {
                mLoadingCount = 0;
                mLoadingDialog.dismiss();
            }
        });
    }

}
