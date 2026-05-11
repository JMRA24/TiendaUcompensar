package com.project.store.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.store.R
import com.project.store.databinding.ActivityForgotPasswordBinding
import com.project.store.utils.MockRepository

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        hideOtpFields()
        setupActions()
    }

    private fun setupActions() {
        binding.backButton.setOnClickListener { finish() }
        binding.sendOtpButton.setOnClickListener { sendOtpMock() }
        binding.resetPasswordButton.setOnClickListener { resetPasswordMock() }
    }

    private fun sendOtpMock() {
        clearErrors()

        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        if (!isEmailValid(email)) return

        val user = MockRepository.findUserByEmail(email)
        if (user == null) {
            binding.emailLayout.error = getString(R.string.error_email_not_found)
            return
        }

        showOtpFields()
        Toast.makeText(
            this,
            getString(R.string.success_otp_sent, getSimulatedOtp()),
            Toast.LENGTH_LONG
        ).show()
    }

    private fun resetPasswordMock() {
        clearErrors()

        val otp = binding.otpInput.text?.toString()?.trim().orEmpty()
        val newPassword = binding.newPasswordInput.text?.toString().orEmpty()
        val confirmPassword = binding.confirmPasswordInput.text?.toString().orEmpty()

        var isValid = true

        when {
            otp.isEmpty() -> {
                binding.otpLayout.error = getString(R.string.error_otp_empty)
                isValid = false
            }
            otp != getSimulatedOtp() -> {
                binding.otpLayout.error = getString(R.string.error_otp_invalid)
                isValid = false
            }
        }

        when {
            newPassword.isEmpty() -> {
                binding.newPasswordLayout.error = getString(R.string.error_password_empty)
                isValid = false
            }
            newPassword.length < MIN_PASSWORD_LENGTH -> {
                binding.newPasswordLayout.error = getString(R.string.error_password_short)
                isValid = false
            }
        }

        if (confirmPassword != newPassword) {
            binding.confirmPasswordLayout.error = getString(R.string.error_password_mismatch)
            isValid = false
        }

        if (!isValid) return

        Toast.makeText(this, R.string.success_password_updated, Toast.LENGTH_SHORT).show()
        finish()
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

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.otpLayout.error = null
        binding.newPasswordLayout.error = null
        binding.confirmPasswordLayout.error = null
    }

    private fun getSimulatedOtp(): String = getString(R.string.simulated_otp_code)

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
