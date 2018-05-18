package com.asksira.webviewsuite;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

/**
 * A combination of WebView and Progressbar.
 * The main aim of this library is to delay the inflation of WebView,
 * So that it does not slow down Activity creation, which happens since Android 5.0.
 * You can have a look at this StackOverflow post:
 * https://stackoverflow.com/questions/46928113/inflating-webview-is-slow-since-lollipop/
 */

public class WebViewSuite extends RelativeLayout {

    private Context context;

    public static final int PROGRESS_BAR_STYLE_NONE = 0;
    public static final int PROGRESS_BAR_STYLE_LINEAR = 1;
    public static final int PROGRESS_BAR_STYLE_CIRCULAR = 2;

    //attributes
    private int progressBarStyle = PROGRESS_BAR_STYLE_LINEAR;
    private int inflationDelay = 100;
    private boolean enableJavaScript = false;
    private boolean overrideTelLink = true;
    private boolean overrideEmailLink = true;
    private boolean showZoomControl = false;
    private boolean enableVerticalScrollBar = false;
    private boolean enableHorizontalScrollBar = false;
    private String url;

    //For loading static data
    private String htmlData;
    private String mimeType;
    private String encoding;

    //View elements
    private ViewStub webViewStub;
    private WebView webView;
    private ProgressBar linearProgressBar;
    private ProgressBar circularProgressBar;
    private ProgressBar customProgressBar;

    private boolean webViewInflated = false;

    private WebViewSuiteCallback callback;
    private WebViewSetupInterference interference;

    public WebViewSuite(@NonNull Context context) {
        super(context);
        init(context);
    }

    public WebViewSuite(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.WebViewSuite, 0, 0);
        try {
            progressBarStyle = a.getInt(R.styleable.WebViewSuite_webViewProgressBarStyle, PROGRESS_BAR_STYLE_LINEAR);
            inflationDelay = a.getInt(R.styleable.WebViewSuite_inflationDelay, 100);
            enableJavaScript = a.getBoolean(R.styleable.WebViewSuite_enableJavaScript, false);
            overrideTelLink = a.getBoolean(R.styleable.WebViewSuite_overrideTelLink, true);
            overrideEmailLink = a.getBoolean(R.styleable.WebViewSuite_overrideEmailLink, true);
            showZoomControl = a.getBoolean(R.styleable.WebViewSuite_showZoomControl, false);
            enableVerticalScrollBar = a.getBoolean(R.styleable.WebViewSuite_enableVerticalScrollBar, false);
            enableHorizontalScrollBar = a.getBoolean(R.styleable.WebViewSuite_enableHorizontalScrollBar, false);
            url = a.getString(R.styleable.WebViewSuite_url);
        } finally {
            a.recycle();
        }
        init(context);
    }

    public WebViewSuite(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        this.context = context;
        View rootView = inflate(context, R.layout.web_view_suite, this);

        webViewStub = rootView.findViewById(R.id.webview_stub);
        linearProgressBar = rootView.findViewById(R.id.linear_progressbar);
        circularProgressBar = rootView.findViewById(R.id.circular_progressbar);

        switch (progressBarStyle) {
            case PROGRESS_BAR_STYLE_CIRCULAR:
                linearProgressBar.setVisibility(GONE);
                circularProgressBar.setVisibility(VISIBLE);
                break;
            case PROGRESS_BAR_STYLE_NONE:
                linearProgressBar.setVisibility(GONE);
                circularProgressBar.setVisibility(GONE);
                break;
            case PROGRESS_BAR_STYLE_LINEAR:
            default:
                circularProgressBar.setVisibility(GONE);
                linearProgressBar.setVisibility(VISIBLE);
        }

        Handler webViewInflationHandler = new Handler();
        webViewInflationHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                webView = (WebView) webViewStub.inflate();
                webViewInflated = true;
                postWebViewInflated();
            }
        }, inflationDelay);

    }

    private void postWebViewInflated () {
        if (!webViewInflated || webView == null) return;
        setupWebView();
        if (url != null && !url.isEmpty()) {
            webView.loadUrl(url);
        } else if (htmlData != null && !htmlData.isEmpty()) {
            webView.loadData(htmlData, mimeType, encoding);
        }
    }

    /**
     * Submit your URL programmatically.
     * This will of course override the URL you set in XML (if any).
     * You can do this in onCreate() of your activity, because even if webView is null,
     * loading will be triggered again after webView is inflated.
     */
    public void startLoading (String url) {
        this.url = url;
        if (!webViewInflated || webView == null) return;
        webView.loadUrl(url);
    }

    public void startLoadData (String data, String mimeType, String encoding) {
        this.htmlData = data;
        this.mimeType = mimeType;
        this.encoding = encoding;
        if (!webViewInflated || webView == null) return;
        webView.loadData(htmlData, mimeType, encoding);
    }

    /**
     * A convenient method for you to override your onBackPressed.
     * return false if there is no more page to goBack / webView is not yet inflated.
     */
    public boolean goBackIfPossible () {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
            return true;
        } else {
            return false;
        }
    }

    /**
     * A convenient method for you to refresh.
     */
    public void refresh () {
        if (webView != null) webView.reload();
    }

    /**
     * If you don't like default progressbar, you can simply submit your own through this method.
     * It will automatically disappear and reappear according to page load.
     */
    public void setCustomProgressBar (ProgressBar progressBar) {
        this.customProgressBar = progressBar;
    }

    public void toggleProgressbar (boolean isVisible) {
        int status = isVisible ? View.VISIBLE : View.GONE;
        switch (progressBarStyle) {
            case PROGRESS_BAR_STYLE_CIRCULAR:
                circularProgressBar.setVisibility(status);
                break;
            case PROGRESS_BAR_STYLE_NONE:
                if (customProgressBar != null) customProgressBar.setVisibility(status);
                break;
            case PROGRESS_BAR_STYLE_LINEAR:
            default:
                linearProgressBar.setVisibility(status);
        }
    }

    /**
     * If you want to customize the behavior of the webViewClient,
     * e.g. Override urls other than default telephone and email,
     * Use this method on WebViewSuite to submit the callbacks.
     * These callbacks will be executed after the codes in WebViewSuite are done.
     */
    public void customizeClient (WebViewSuiteCallback callback) {
        this.callback = callback;
    }

    /**
     * If you want to customize the settings of the webViewClient,
     * You cannot do it directly in onCreate() of your activity by getting WebView from WebViewSuite.
     * Why? Because the main point of this library is to delay the inflation - WebView is null in onCreate()!
     *
     * Therefore, I provided a callback for you to submit your own settings.
     * Use this method on WebViewSuite (This time in onCreate()) and submit the callback.
     * This callback will be executed after the default settings in WebViewSuite are completed.
     * I can assure you that webView is not null during interfereWebViewSetup().
     */
    public void interfereWebViewSetup (WebViewSetupInterference interference) {
        this.interference = interference;
    }

    public WebView getWebView () {
        return this.webView;
    }

    public ProgressBar getProgressBar (int progressBarStyle) {
        return progressBarStyle == PROGRESS_BAR_STYLE_LINEAR ? linearProgressBar : circularProgressBar;
    }

    private void setupWebView () {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                toggleProgressbar(true);
                if (callback != null) callback.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                toggleProgressbar(false);
                if (callback != null) callback.onPageFinished(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url.startsWith("tel:") && overrideTelLink) {
                    try {
                        Intent telIntent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
                        context.startActivity(telIntent);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                } else if (url.startsWith("mailto:") && overrideEmailLink) {
                    try {
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{url.substring(7)});
                        if (emailIntent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(emailIntent);
                        }
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                } else {
                    if (callback != null) {
                        return callback.shouldOverrideUrlLoading(webView, url);
                    } else {
                        return super.shouldOverrideUrlLoading(view, url);
                    }
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(enableJavaScript);
        webSettings.setBuiltInZoomControls(showZoomControl);
        webView.setVerticalScrollBarEnabled(enableVerticalScrollBar);
        webView.setHorizontalScrollBarEnabled(enableHorizontalScrollBar);
        if (interference != null) interference.interfereWebViewSetup(webView);
    }

    public interface WebViewSuiteCallback {
        void onPageStarted(WebView view, String url, Bitmap favicon);
        void onPageFinished(WebView view, String url);
        boolean shouldOverrideUrlLoading (WebView view, String url);
    }

    public interface WebViewSetupInterference {
        void interfereWebViewSetup (WebView webView);
    }

}
