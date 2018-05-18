# WebViewSuite

A WebView that:

1. **Does not delay Activity creation** (See [this post](https://stackoverflow.com/questions/46928113/inflating-webview-is-slow-since-lollipop/))
2. Built-in ProgressBars (You can also override using your own ProgressBar)
3. Configure WebView client through XML
4. Largely reduce your code needed in creating a simple WebView

## Usage

### Step 1: Add to Project

First make sure `jcenter()` is included as a repository in your **project**'s build.gradle:  

```
allprojects {
    repositories {
        google()
        jcenter()
    }
}
```

And then add the below to your app's build.gradle:  

```
    implementation 'com.asksira.android:webviewsuite:1.0.2'
```

### Step 2: Add WebViewSuite to XML

```xml
    <com.asksira.webviewsuite.WebViewSuite
        android:id="@+id/webViewSuite"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:url="@string/url"
        app:webViewProgressBarStyle="linear"
        app:inflationDelay="100"
        app:enableJavaScript="false"
        app:overrideTelLink="true"
        app:overrideEmailLink="true"
        app:showZoomControl="false"
        app:enableVerticalScrollBar="false"
        app:enableHorizontalScrollBar="false"/>
```

| Attribute Name             | Default      | Allowed Values                |
|:---------------------------|:-------------|:------------------------------|
| webViewProgressBarStyle    | linear       | linear / circular / none      |
| inflationDelay             | 100          | any integer (represent ms)    |
| enableJavaScript           | false        | true / false                  |
| overrideTelLink            | true         | true / false                  | 
| overrideEmailLink          | true         | true / false                  | 
| showZoomControl            | false        | true / false                  | 
| enableVerticalScrollBar    | false        | true / false                  | 
| enableHorizontalScrollBar  | false        | true / false                  | 
| url                        | (emptyString)| any String                    | 

### Override onBackPressed() in your Activity (Suggested)

```java
    @Override
    public void onBackPressed() {
        if (!webViewSuite.goBackIfPossible()) super.onBackPressed();
    }
```

## Advanced Usage

### Load URL Programmatically

```java
webViewSuite.startLoading(myURL);
```

You don't need to worry about WebView not being inflated here.  
If WebView is not yet inflated, `myURL` will be loaded automatically after WebView is inflated.

### I want to use my own ProgressBar

```java
webViewSuite.setCustomProgressBar (myProgressBar);
```

`myProgressBar` will automatically change visibility with page loads.

### I want to bind a refresh button to the WebView

```java
webViewSuite.refresh()
```

### Customizing WebViewClient

This is needed if you need to add more behavior to the WebViewClient.  
The most common use-case is to override more URLs according to your project needs.

```java
        webViewSuite.customizeClient(new WebViewSuite.WebViewSuiteCallback() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                //Do your own stuffs. These will be executed after default onPageStarted().
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                //Do your own stuffs. These will be executed after default onPageFinished().
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //Override those URLs you need and return true.
                //Return false if you don't need to override that URL.
            }
        });
```

### Customizing WebView Settings

WebView has a bunch of settings.  
If you need to change some of them, DO NOT call getWebView() and change its settings directly. This is because webView may not yet been inflated.  
Instead, use the below callback:

```java
        webViewSuite.interfereWebViewSetup(new WebViewSuite.WebViewSetupInterference() {
            @Override
            public void interfereWebViewSetup(WebView webView) {
                WebSettings webSettings = webView.getSettings();
                //Change your WebView settings here
            }
        });
    }
```

### Loading static HTML data

Instead of `startLoading()`, use `startLoadData()` instead:

```java
webViewSuite.startLoadData(data, mimeType, encoding);
```


## How does it work?

Since Android 5.0, the first time inflation of a WebView is very slow.  
If you do not delay its inflation, It will freeze the user when user is trying to open an Activity that has a WebView. See [this post](https://stackoverflow.com/questions/46928113/inflating-webview-is-slow-since-lollipop) for more information.

By using [`ViewStub`](https://developer.android.com/reference/android/view/ViewStub.html) with `Handler.postDelayed()`, the WebView is inflated after the Activity is completely created and **visible to user**. So when the WebView is inflating, what user sees is a created Activity with running progressbar. It perfectly **looks like the time needed for the inflation of WebView is just the time needed to load that webpage**.

This trick greatly improved user experience.  
Actually, such trick is not only applicable to WebView, but applicable to all View elements that delays Activity creation due to slow inflation.

## Release Notes

v1.0.2
- Flattened View Hierarchy
- Made `toggleProgressBar()` public

v1.0.1  
- Added support for static HTML data loading

## License

```
Copyright 2017 Sira Lam

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and 
associated documentation files (the WebViewSuite), to deal in the Software without restriction,
including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or 
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
```