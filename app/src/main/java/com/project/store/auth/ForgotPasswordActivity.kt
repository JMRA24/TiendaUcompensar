package com.project.store.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.project.store.R
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.ActivityForgotPasswordBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private val repository = FirebaseRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideOtpFields()
        setupActions()
    }

    private fun setupActions() {
        binding.backButton.setOnClickListener { finish() }
        binding.sendOtpButton.setOnClickListener { sendPasswordResetEmail() }
        binding.resetPasswordButton.setOnClickListener { sendPasswordResetEmail() }
    }

    private fun sendPasswordResetEmail() {
        clearErrors()

        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        if (!isEmailValid(email)) return

        setLoading(true)
        lifecycleScope.launch {
            try {
                repository.auth.sendPasswordResetEmail(email).await()
                Toast.makeText(
                    this@ForgotPasswordActivity,
                    R.string.success_otp_sent,
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } catch (e: Exception) {
                binding.emailLayout.error = e.localizedMessage ?: getString(R.string.error_unknown)
            } finally {
                setLoading(false)
            }
        }
    }

    private fun isEmailValid(email: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.emailLayout.error = getString(R.string.error_email_empty)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = getString(R.string.error_email_invalid)
                false
            }
            else -> true
        }
    }

    private fun showOtpFields() {
        binding.otpSection.visibility = View.VISIBLE
        binding.resetPasswordButton.visibility = View.VISIBLE
        binding.sendOtpButton.isEnabled = false
    }

    private fun hideOtpFields() {
        binding.otpSection.visibility = View.GONE
        binding.resetPasswordButton.visibility = View.GONE
    }

    private fun setLoading(isLoading: Boolean) {
        binding.sendOtpButton.isEnabled = !isLoading
        binding.resetPasswordButton.isEnabled = !isLoading
        binding.emailInput.isEnabled = !isLoading
    }

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.otpLayout.error = null
        binding.newPasswordLayout.error = null
        binding.confirmPasswordLayout.error = null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
