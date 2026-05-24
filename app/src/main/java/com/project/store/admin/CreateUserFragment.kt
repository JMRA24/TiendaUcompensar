package com.project.store.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentCreateUserBinding
import com.project.store.models.UserRole
import kotlinx.coroutines.launch

class CreateUserFragment : Fragment() {

    private var _binding: FragmentCreateUserBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.deleteUserButton.visibility = View.GONE
        binding.saveUserButton.setOnClickListener {
            if (validateForm()) {
                createUser()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun validateForm(): Boolean {
        clearErrors()
        var isValid = true
        val name = binding.userNameInput.text?.toString()?.trim().orEmpty()
        val email = binding.userEmailInput.text?.toString()?.trim().orEmpty()
        val phone = binding.userPhoneInput.text?.toString()?.trim().orEmpty()
        val password = binding.userPasswordInput.text?.toString().orEmpty()
        val role = selectedRole()

        if (name.isEmpty()) {
            binding.userNameLayout.error = getString(R.string.error_user_name_required)
            isValid = false
        }

        when {
            email.isEmpty() -> {
                binding.userEmailLayout.error = getString(R.string.error_user_email_required)
                isValid = false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.userEmailLayout.error = getString(R.string.error_user_email_invalid)
                isValid = false
            }
        }

        if (phone.isEmpty()) {
            binding.userPhoneLayout.error = getString(R.string.error_user_phone_required)
            isValid = false
        }

        when {
            password.isEmpty() -> {
                binding.userPasswordLayout.error = getString(R.string.error_user_password_required)
                isValid = false
            }
            password.length < MIN_PASSWORD_LENGTH -> {
                binding.userPasswordLayout.error = getString(R.string.error_user_password_short)
                isValid = false
            }
        }

        if (role == null) {
            binding.userRoleError.setText(R.string.error_user_role_required)
            isValid = false
        }

        return isValid
    }

    private fun selectedRole(): UserRole? = when (binding.userRoleGroup.checkedRadioButtonId) {
        R.id.roleAdmin -> UserRole.ADMIN
        R.id.roleSeller -> UserRole.SELLER
        R.id.roleBuyer -> UserRole.BUYER
        else -> null
    }

    private fun createUser() {
        val name = binding.userNameInput.text?.toString()?.trim().orEmpty()
        val email = binding.userEmailInput.text?.toString()?.trim().orEmpty()
        val phone = binding.userPhoneInput.text?.toString()?.trim().orEmpty()
        val password = binding.userPasswordInput.text?.toString().orEmpty()
        val role = selectedRole()?.toFirebaseRole() ?: return

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.registerUser(email, password, name, role)
                .onSuccess { user ->
                    repository.updateUserProfile(user.copy(phone = phone))
                    Toast.makeText(requireContext(), R.string.user_created_success, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                .onFailure {
                    Toast.makeText(requireContext(), R.string.error_auth, Toast.LENGTH_SHORT).show()
                }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.saveUserButton.isEnabled = !isLoading
        binding.userNameInput.isEnabled = !isLoading
        binding.userEmailInput.isEnabled = !isLoading
        binding.userPhoneInput.isEnabled = !isLoading
        binding.userPasswordInput.isEnabled = !isLoading
        binding.userRoleGroup.isEnabled = !isLoading
    }

    private fun UserRole.toFirebaseRole(): String = when (this) {
        UserRole.ADMIN -> "admin"
        UserRole.SELLER -> "seller"
        UserRole.BUYER -> "buyer"
    }

    private fun clearErrors() {
        binding.userNameLayout.error = null
        binding.userEmailLayout.error = null
        binding.userPhoneLayout.error = null
        binding.userPasswordLayout.error = null
        binding.userRoleError.text = null
    }

    companion object {
        private const val MIN_PASSWORD_LENGTH = 6
    }
}
