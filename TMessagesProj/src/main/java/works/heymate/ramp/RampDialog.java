package works.heymate.ramp;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;

public class RampDialog extends Dialog {

    private static final String EXIT_URL = "https://heymate.works";

    public interface OnRampDoneListener {

        void onRampDone();

    }

    private String mURL;

    private WebView mWebView;

    private OnRampDoneListener mListener;

    public RampDialog(@NonNull Context context, String url, OnRampDoneListener listener) {
        super(context);
        setCancelable(true);

        mURL = url;

        mListener = listener;
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
                if (url.toLowerCase().startsWith(EXIT_URL)) {
                    dismiss();
                    mListener.onRampDone();
                    return true;
                }

                return false;
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
                    int fontSize = 20;
                    int proceedFontSize = 16;

                    String code =
                            "document.getElementById('root').children[0].style.width='" + width + "px';" +
                            "document.getElementsByClassName('style__sellBuyToggle--2BcMw')[0].style.display='none';" +
                            "document.getElementsByClassName('style__poweredByRamp--8Vb21')[0].style.display = 'none';" +
                            "[].forEach.call(document.getElementsByClassName('style__amountInput--1zk6X'), function(elem) {elem.style.fontSize='" + fontSize + "px';});" +
                            "document.getElementsByClassName('style__proceedButton--k6MOS')[0].style.fontSize='" + proceedFontSize + "px';";

                String observerScript = "document.addEventListener('DOMNodeInserted', function() {if (document.getElementById('qazxsw')) {" + code + "}}, false);";
                String immediateScript = "javascript:(function() {" + code + "})()";

                view.loadUrl(immediateScript);

//                    view.loadUrl(
//                            "javascript:(function() {" +
//                                    "document.getElementById('root').children[0].style.width='" + width + "px';" +
//                                    "document.getElementsByClassName('style__sellBuyToggle--2BcMw')[0].style.display='none';" +
//                                    "document.getElementsByClassName('style__poweredByRamp--8Vb21')[0].style.display = 'none';" +
//                                    "[].forEach.call(document.getElementsByClassName('style__amountInput--1zk6X'), function(elem) {elem.style.fontSize='" + fontSize + "px';});" +
//                                    "document.getElementsByClassName('style__proceedButton--k6MOS')[0].style.fontSize='" + proceedFontSize + "px';" +
//                                    "})()");
                }, 2000);
            }

        });

        setContentView(mWebView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.displaySize.y / 10 * 9));
    }

}
