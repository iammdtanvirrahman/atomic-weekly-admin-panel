package com.atomicweekly.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var refresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 9, 12)
        window.navigationBarColor = Color.rgb(8, 9, 12)

        refresh = SwipeRefreshLayout(this).apply {
            setColorSchemeColors(Color.rgb(240, 68, 82))
            setProgressBackgroundColorSchemeColor(Color.rgb(17, 19, 26))
        }

        webView = WebView(this).apply {
            setBackgroundColor(Color.rgb(8, 9, 12))
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.loadsImagesAutomatically = true
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.javaScriptCanOpenWindowsAutomatically = false
            settings.setSupportMultipleWindows(false)
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    val isAdminSite = uri.scheme == "https" &&
                        (uri.host == "iammdtanvirrahman.github.io" ||
                         uri.host == "atomic-tanvir.firebaseapp.com" ||
                         uri.host == "atomictanvir.firebaseapp.com")
                    return if (isAdminSite) false else {
                        runCatching { startActivity(Intent(Intent.ACTION_VIEW, uri)) }
                        true
                    }
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    refresh.isRefreshing = false
                }
            }
            webChromeClient = WebChromeClient()
        }

        refresh.addView(webView, FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        ))
        refresh.setOnRefreshListener { webView.reload() }
        setContentView(refresh)

        if (savedInstanceState != null) webView.restoreState(savedInstanceState)
        else webView.loadUrl(ADMIN_URL)
    }

    @Deprecated("Deprecated in Android API; retained for consistent back navigation")
    override fun onBackPressed() {
        if (::webView.isInitialized && webView.canGoBack()) webView.goBack()
        else super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (::webView.isInitialized) webView.saveState(outState)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        if (::webView.isInitialized) webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val ADMIN_URL = "https://iammdtanvirrahman.github.io/atomic-weekly-admin-panel/"
    }
}
