package com.atomicweekly.admin

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private lateinit var refresh: SwipeRefreshLayout
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(8, 9, 12)
        window.navigationBarColor = Color.rgb(8, 9, 12)
        credentialManager = CredentialManager.create(this)

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
            addJavascriptInterface(NativeAuthBridge(), "AndroidAuth")
            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                    val uri = request.url
                    val isAdminSite = uri.scheme == "https" &&
                        (uri.host == "iammdtanvirrahman.github.io" ||
                         uri.host == "atomictanvir.firebaseapp.com" ||
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

    private inner class NativeAuthBridge {
        @JavascriptInterface
        fun requestGoogleSignIn() {
            runOnUiThread {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        val webClientId = getString(R.string.default_web_client_id)
                        require(webClientId.isNotBlank() && !webClientId.startsWith("REPLACE_")) {
                            "Google Web Client ID is not configured. Add the OAuth Web Client ID in strings.xml."
                        }
                        val googleIdOption = GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId(webClientId)
                            .setAutoSelectEnabled(false)
                            .build()
                        val request = GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()
                        val result = credentialManager.getCredential(
                            context = this@MainActivity,
                            request = request
                        )
                        val credential = result.credential
                        if (credential is androidx.credentials.CustomCredential &&
                            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                        ) {
                            val googleToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                            val js = "window.onNativeGoogleToken && window.onNativeGoogleToken(${JSONObject.quote(googleToken)});"
                            webView.evaluateJavascript(js, null)
                        } else {
                            showAuthError("Google returned an unsupported sign-in credential.")
                        }
                    } catch (error: GetCredentialException) {
                        showAuthError(error.message ?: "Google sign-in was cancelled or unavailable.")
                    } catch (error: Exception) {
                        showAuthError(error.message ?: "Unable to start Google sign-in.")
                    }
                }
            }
        }
    }

    private fun showAuthError(message: String) {
        val js = "window.onNativeGoogleSignInError && window.onNativeGoogleSignInError(${JSONObject.quote(message)});"
        runOnUiThread {
            webView.evaluateJavascript(js, null)
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
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