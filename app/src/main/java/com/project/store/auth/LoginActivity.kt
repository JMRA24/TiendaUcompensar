package com.project.store.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.project.store.R
import com.project.store.admin.AdminActivity
import com.project.store.buyer.BuyerActivity
import com.project.store.databinding.ActivityLoginBinding
import com.project.store.models.UserRole
import com.project.store.seller.SellerActivity
import com.project.store.utils.MockRepository

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActions()
    }

    private fun setupActions() {
        binding.loginButton.setOnClickListener { loginWithCredentials() }
        binding.biometricButton.setOnClickListener { loginWithBiometricMock() }
        binding.googleLoginButton.setOnClickListener { loginWithGoogleMock() }
        binding.forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginWithCredentials() {
        clearErrors()

        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()

        if (!isFormValid(email, password)) return

        val user = MockRepository.findUserByEmail(email)
        if (user != null && user.password == password && user.isActive) {
            showLoginSuccess(user.fullName)
            navigateToRoleHome(user.role)
        } else {
            binding.passwordLayout.error = getString(R.string.error_invalid_credentials)
        }
    }

    private fun loginWithBiometricMock() {
        val mockBuyer = MockRepository.getUsersByRole(UserRole.BUYER).first()
        Toast.makeText(
            this,
            getString(R.string.biometric_login_success, mockBuyer.fullName),
            Toast.LENGTH_SHORT
        ).show()
        navigateToRoleHome(mockBuyer.role)
    }

    private fun loginWithGoogleMock() {
        Toast.makeText(
            this,
            R.string.google_login_simulated,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun isFormValid(email: String, password: String): Boolean {
        var isValid = true

        when {
            email.isEmpty() -> {
                binding.emailLayout.error = getString(R.string.error_email_empty)
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.emailLayout.error = getString(R.string.error_email_invalid)
                isValid = false
            }
        }

        when {
            password.isEmpty() -> {
                binding.passwordLayout.error = getString(R.string.error_password_empty)
                isValid = false
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                binding.passwordLayout.error = getString(R.string.error_password_short)
                isValid = false
            }
        }

        return isValid
    }

    private fun clearErrors() {
        binding.emailLayout.error = null
        binding.passwordLayout.error = null
    }

    private fun showLoginSuccess(fullName: String) {
        Toast.makeText(
            this,
            getString(R.string.login_success_simulated, fullName),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToRoleHome(role: UserRole) {
        val destination = when (role) {
            UserRole.ADMIN -> AdminActivity::class.java
            UserRole.SELLER -> SellerActivity::class.java
            UserRole.BUYER -> BuyerActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
