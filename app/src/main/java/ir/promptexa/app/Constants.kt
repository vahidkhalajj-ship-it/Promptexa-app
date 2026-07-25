package ir.promptexa.app

/**
 * Central place for site URLs and third-party config placeholders.
 * Do NOT hardcode real secrets here in a public repo — see README
 * for how to inject these safely via local.properties / BuildConfig.
 */
object Constants {

    const val BASE_URL = "https://www.promptexa.ir"
    const val BRAND_REDIRECT_URL = "https://www.ptxplus.ir"

    const val URL_HOME = "https://www.promptexa.ir/"
    const val URL_PTX_PLUS = "https://www.promptexa.ir/%d9%be%db%8c-%d8%aa%db%8c-%d8%a7%db%8c%da%a9%d8%b3-%d9%be%d9%84%d8%b3/"
    const val URL_ACADEMY = "https://www.promptexa.ir/%d8%a2%da%a9%d8%a7%d9%85%db%8c-%d9%be%d8%b1%d8%a7%d9%85%d9%be%d8%aa%da%a9%d8%b3%d8%a7/"
    const val URL_CONTACT = "https://www.promptexa.ir/%d8%aa%d9%85%d8%a7%d8%b3-%d8%a8%d8%a7-%d9%85%d8%a7/"
    const val URL_ABOUT = "https://www.promptexa.ir/%d8%af%d8%b1%d8%a8%d8%a7%d8%b1%d9%87-%d9%85%d8%a7/"
    const val URL_MY_ACCOUNT = "https://www.promptexa.ir/my-account/"
    const val URL_SUPPORT = "https://www.promptexa.ir/my-account/support/"
    const val URL_SHOP = "https://www.promptexa.ir/shop/"
    const val URL_CATEGORIES = "https://www.promptexa.ir/product-category/%D9%BE%D8%B1%D8%A7%D9%85%D9%BE%D8%AA-%D9%87%D8%A7/"
    const val URL_CART = "https://www.promptexa.ir/cart/"

    // ---- Webpushr (do not hardcode real values in production builds) ----
    const val WEBPUSHR_API_KEY = "YOUR_WEBPUSHR_API_KEY"
    const val WEBPUSHR_AUTH_TOKEN = "YOUR_WEBPUSHR_AUTH_TOKEN"

    // Notification intent extra keys
    const val EXTRA_NOTIFICATION_URL = "extra_notification_url"
}
