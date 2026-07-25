package ir.promptexa.app

import android.app.Application

/**
 * Intentionally minimal Application class.
 * Per spec: no local database, no extra frameworks, low memory footprint.
 */
class PromptexaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Nothing heavy here on purpose. Firebase auto-initializes via
        // the google-services plugin / ContentProvider, no manual init needed.
    }
}
