package com.project.store.auth

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.project.store.R
import com.project.store.admin.AdminActivity
import com.project.store.buyer.BuyerActivity
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.ActivityLoginBinding
import com.project.store.seller.SellerActivity
import com.project.store.utils.BiometricHelper
import com.project.store.utils.SecurePreferences
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val repository = FirebaseRepository.getInstance()
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (repository.isLoggedIn()) {
            if (BiometricHelper.checkAvailability(this) == BiometricHelper.BiometricStatus.AVAILABLE &&
                SecurePreferences.isBiometricEnabled(this) &&
                !SecurePreferences.isJustLoggedOut(this)
            ) {
                loginWithBiometric()
                return
            }
            checkCurrentUserAndNavigate()
            return
        }

        setupActions()
    }

    private fun setupActions() {
        SecurePreferences.setJustLoggedOut(this, false)
        binding.loginButton.setOnClickListener { loginWithCredentials() }
        binding.biometricButton.setOnClickListener { loginWithBiometric() }
        binding.googleLoginButton.setOnClickListener { loginWithGoogle() }
        binding.forgotPasswordButton.setOnClickListener {
            startActivity(Intent(this, ForgotPasswordActivity::class.java))
        }
    }

    private fun loginWithCredentials() {
        clearErrors()
        val email = binding.emailInput.text?.toString()?.trim().orEmpty()
        val password = binding.passwordInput.text?.toString().orEmpty()
        if (!isFormValid(email, password)) return

        showLoading(true)
        lifecycleScope.launch {
            val result = repository.loginWithEmail(email, password)
            showLoading(false)
            result.fold(
                onSuccess = { user ->
                    Toast.makeText(
                        this@LoginActivity,
                        getString(R.string.login_success_simulated, user.name),
                        Toast.LENGTH_SHORT
                    ).show()
                    SecurePreferences.saveUserSession(this@LoginActivity, user.id, user.role, user.name)
                    SecurePreferences.setBiometricEnabled(this@LoginActivity, true)
                    navigateByRole(user.role)
                },
                onFailure = {
                    binding.passwordLayout.error = getString(R.string.error_invalid_credentials)
                }
            )
        }
    }

    private fun loginWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(this, gso)
        startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken ?: return
                showLoading(true)
                lifecycleScope.launch {
                    val result = repository.loginWithGoogle(idToken)
                    showLoading(false)
                    result.fold(
                        onSuccess = { user -> navigateByRole(user.role) },
                        onFailure = {
                            Toast.makeText(
                                this@LoginActivity,
                                getString(R.string.error_auth),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            } catch (e: ApiException) {
                Toast.makeText(this, getString(R.string.error_auth), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginWithBiometric() {
        val status = BiometricHelper.checkAvailability(this)
        if (status != BiometricHelper.BiometricStatus.AVAILABLE) {
            Toast.makeText(
                this,
                getString(R.string.biometric_not_available),
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        BiometricHelper.showPrompt(
            activity = this,
            title = getString(R.string.biometric_title),
            subtitle = getString(R.string.biometric_subtitle),
            negativeText = getString(R.string.biometric_negative),
            onSuccess = {
                val role = SecurePreferences.getUserRole(this)
                navigateByRole(role ?: "buyer")
            },
            onError = { msg ->
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            },
            onFailed = {
                Toast.makeText(
                    this,
                    getString(R.string.error_auth),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun checkCurrentUserAndNavigate() {
        lifecycleScope.launch {
            // Solo navega si hay sesion valida en Firestore
            val firebaseUser = repository.getCurrentFirebaseUser()
            if (firebaseUser != null) {
                val doc = repository.usersCollection
                    .document(firebaseUser.uid)
                    .get()
                    .await()
                val role = doc.getString("role") ?: "buyer"
                navigateByRole(role)
            }
        }
    }

    private fun navigateByRole(role: String) {
        val destination = when (role) {
            "admin" -> AdminActivity::class.java
            "seller" -> SellerActivity::class.java
            else -> BuyerActivity::class.java
        }
        startActivity(Intent(this, destination))
        finish()
    }

    private fun showLoading(show: Boolean) {
        binding.loginButton.isEnabled = !show
        binding.googleLoginButton.isEnabled = !show
        binding.biometricButton.isEnabled = !show
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

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
