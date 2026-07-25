package ir.promptexa.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import ir.promptexa.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Tracks whether the currently loaded page is one of the 4 bottom-nav "home" pages,
    // used to decide whether Back should exit the app or navigate the WebView history.
    private var lastLoadedUrl: String = Constants.URL_HOME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupWebView()
        setupBottomNav()
        setupSwipeRefresh()
        setupBackPressHandling()

        val startUrl = intent?.getStringExtra(Constants.EXTRA_NOTIFICATION_URL)
            ?: Constants.URL_HOME

        if (isNetworkAvailable()) {
            loadUrl(startUrl)
        } else {
            showOffline()
        }

        binding.retryButton.setOnClickListener {
            if (isNetworkAvailable()) {
                hideOffline()
                loadUrl(lastLoadedUrl)
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        val webView = binding.webView
        val settings: WebSettings = webView.settings

        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.databaseEnabled = true
        settings.cacheMode = WebSettings.LOAD_DEFAULT
        settings.setSupportZoom(false)
        settings.builtInZoomControls = false
        settings.loadWithOverviewMode = true
        settings.useWideViewPort = true
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE

        // Keep the user's WooCommerce/WordPress login session between app launches.
        CookieManager.getInstance().setAcceptCookie(true)
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true)

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                binding.progressBar.progress = newProgress
                binding.progressBar.visibility = if (newProgress in 1..99) View.VISIBLE else View.GONE
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url ?: return false
                // Keep all promptexa.ir / ptxplus.ir navigation inside the app.
                val host = url.host ?: ""
                return if (host.endsWith("promptexa.ir") || host.endsWith("ptxplus.ir")) {
                    false // let the WebView load it
                } else {
                    // External links (e.g. payment gateways, social links) open in system browser.
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, url))
                    } catch (_: Exception) { }
                    true
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.swipeRefresh.isRefreshing = false
                url?.let { lastLoadedUrl = it }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                if (request?.isForMainFrame == true) {
                    showOffline()
                }
            }
        }
    }

    private fun setupBottomNav() {
        binding.bottomNav.setOnItemSelectedListener { item ->
            val url = when (item.itemId) {
                R.id.nav_home -> Constants.URL_HOME
                R.id.nav_categories -> Constants.URL_CATEGORIES
                R.id.nav_cart -> Constants.URL_CART
                R.id.nav_account -> Constants.URL_MY_ACCOUNT
                else -> Constants.URL_HOME
            }
            if (isNetworkAvailable()) {
                loadUrl(url)
            } else {
                showOffline()
            }
            true
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            if (isNetworkAvailable()) {
                binding.webView.reload()
            } else {
                binding.swipeRefresh.isRefreshing = false
                showOffline()
            }
        }
    }

    private fun setupBackPressHandling() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when {
                    binding.webView.canGoBack() && !isOnHomePage() -> binding.webView.goBack()
                    else -> showExitConfirmationDialog()
                }
            }
        })
    }

    private fun isOnHomePage(): Boolean {
        val normalized = lastLoadedUrl.trimEnd('/')
        return normalized == Constants.URL_HOME.trimEnd('/') ||
            normalized == "https://www.promptexa.ir"
    }

    private fun showExitConfirmationDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.exit_dialog_message))
            .setNegativeButton(getString(R.string.exit_dialog_cancel), null)
            .setPositiveButton(getString(R.string.exit_dialog_exit)) { _, _ -> finishAffinity() }
            .show()
    }

    private fun loadUrl(url: String) {
        hideOffline()
        binding.webView.loadUrl(url)
    }

    private fun showOffline() {
        binding.offlineLayout.visibility = View.VISIBLE
        binding.swipeRefresh.visibility = View.GONE
    }

    private fun hideOffline() {
        binding.offlineLayout.visibility = View.GONE
        binding.swipeRefresh.visibility = View.VISIBLE
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val notificationUrl = intent.getStringExtra(Constants.EXTRA_NOTIFICATION_URL)
        if (!notificationUrl.isNullOrEmpty() && isNetworkAvailable()) {
            loadUrl(notificationUrl)
        }
    }
}
