package works.heymate.ramp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;

public class RampDialog extends Dialog {

    private String mURL;

    private WebView mWebView;

    public RampDialog(@NonNull Context context, String url) {
        super(context);
        setCancelable(true);

        mURL = url;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mWebView = new WebView(getContext());
        mWebView.getSettings().setJavaScriptEnabled(true);
        mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        mWebView.getSettings().setBuiltInZoomControls(false);
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.getSettings().setLoadsImagesAutomatically(true);
        mWebView.getSettings().setSupportZoom(false);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);

        mWebView.loadUrl(mURL);

        mWebView.setWebViewClient(new WebViewClient() {

            boolean jsInjectPending = true;

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (mURL.equalsIgnoreCase(url)) {
                    return false;
                }

                dismiss();
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (!jsInjectPending) {
                    return;
                }

                jsInjectPending = false;

                new Handler().postDelayed(() -> {
                    int width = (int) (view.getWidth() / AndroidUtilities.displayMetrics.density);
                    int fontSize = 18;

                    view.loadUrl(
                            "javascript:(function() {" +
                                    "document.getElementById('root').children[0].style.width='" + width + "px';" +
                                    "document.getElementsByClassName('style__sellBuyToggle--2BcMw')[0].style.display='none';" +
                                    "document.getElementsByClassName('style__poweredByRamp--8Vb21')[0].style.display = 'none';" +
                                    "[].forEach.call(document.getElementsByClassName('style__amountInput--1zk6X'), function(elem) {elem.style.fontSize='" + fontSize + "px';});" +
                                    "})()");
                }, 5000);
            }

        });

        setContentView(mWebView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.displaySize.y / 10 * 9));
    }

}
