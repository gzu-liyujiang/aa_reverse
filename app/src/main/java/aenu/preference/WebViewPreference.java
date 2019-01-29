package aenu.preference;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import aenu.reverse.ui.R;

public class WebViewPreference extends DialogPreference {
    private static final String NAMESPACE = "http://schemas.android.com/apk/aenu.preference";
    private String url;
    private WebView webView;

    public WebViewPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        url = attributeSet.getAttributeValue(NAMESPACE, "url");
        setDialogLayoutResource(R.layout.preference_webview);
        setNegativeButtonText(R.string.hide);
    }

    @Override
    public void onPrepareDialogBuilder(Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton((CharSequence) null, (OnClickListener) null);
    }

    @Override
    public void onBindDialogView(View view) {
        super.onBindDialogView(view);
        webView = (WebView) view;
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebChromeClient(new WebChromeClient(){
            public void onReceivedTitle(WebView view,String title){
                getDialog().setTitle(title);
            }
        });
        this.webView.loadUrl(this.url);
    }
}

