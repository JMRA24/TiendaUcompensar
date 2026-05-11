package com.project.store.auth

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.project.store.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val handler = Handler(Looper.getMainLooper())

    private val navigateToLogin = Runnable {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        animateBrand()
        handler.postDelayed(navigateToLogin, SPLASH_DELAY_MILLIS)
    }

    override fun onDestroy() {
        handler.removeCallbacks(navigateToLogin)
        super.onDestroy()
    }

    private fun animateBrand() {
        binding.logoImage.alpha = INITIAL_ALPHA
        binding.logoImage.scaleX = INITIAL_SCALE
        binding.logoImage.scaleY = INITIAL_SCALE
        binding.logoImage.animate()
            .alpha(FINAL_ALPHA)
            .scaleX(FINAL_SCALE)
            .scaleY(FINAL_SCALE)
            .setDuration(LOGO_ANIMATION_MILLIS)
            .start()

        binding.appName.alpha = INITIAL_ALPHA
        binding.appTagline.alpha = INITIAL_ALPHA
        binding.splashLoading.alpha = INITIAL_ALPHA

        binding.appName.animate()
            .alpha(FINAL_ALPHA)
            .setStartDelay(TEXT_ANIMATION_DELAY_MILLIS)
            .setDuration(TEXT_ANIMATION_MILLIS)
            .start()

        binding.appTagline.animate()
            .alpha(FINAL_ALPHA)
            .setStartDelay(TEXT_ANIMATION_DELAY_MILLIS)
            .setDuration(TEXT_ANIMATION_MILLIS)
            .start()

        binding.splashLoading.animate()
            .alpha(FINAL_ALPHA)
            .setStartDelay(LOADING_ANIMATION_DELAY_MILLIS)
            .setDuration(TEXT_ANIMATION_MILLIS)
            .start()
    }

    companion object {
        private const val SPLASH_DELAY_MILLIS = 1800L
        private const val LOGO_ANIMATION_MILLIS = 700L
        private const val TEXT_ANIMATION_MILLIS = 500L
        private const val TEXT_ANIMATION_DELAY_MILLIS = 250L
        private const val LOADING_ANIMATION_DELAY_MILLIS = 500L
        private const val INITIAL_ALPHA = 0f
        private const val FINAL_ALPHA = 1f
        private const val INITIAL_SCALE = 0.86f
        private const val FINAL_SCALE = 1f
    }
}
