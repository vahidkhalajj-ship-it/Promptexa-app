package ir.promptexa.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import ir.promptexa.app.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val splashDurationMs = 1400L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fade in + slight scale up animation (per spec: 1-2 seconds total)
        val fadeIn = ObjectAnimator.ofFloat(binding.splashLogo, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(binding.splashLogo, "scaleX", 0.85f, 1f)
        val scaleY = ObjectAnimator.ofFloat(binding.splashLogo, "scaleY", 0.85f, 1f)

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY)
            duration = 600
            interpolator = DecelerateInterpolator()
            start()
        }

        // Carry forward any deep-link/notification extras to MainActivity
        val notificationUrl = intent?.getStringExtra(Constants.EXTRA_NOTIFICATION_URL)

        binding.root.postDelayed({
            val mainIntent = Intent(this, MainActivity::class.java)
            if (!notificationUrl.isNullOrEmpty()) {
                mainIntent.putExtra(Constants.EXTRA_NOTIFICATION_URL, notificationUrl)
            }
            startActivity(mainIntent)
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }, splashDurationMs)
    }
}
