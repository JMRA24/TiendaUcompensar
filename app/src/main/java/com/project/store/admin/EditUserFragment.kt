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
import com.project.store.data.model.User as FirebaseUserModel
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentEditUserBinding
import com.project.store.models.UserRole
import kotlinx.coroutines.launch

class EditUserFragment : Fragment() {

    private var _binding: FragmentEditUserBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private var user: FirebaseUserModel? = null
    private var requestedUserId: Int = DEFAULT_USER_ID

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestedUserId = arguments?.getInt(ARG_USER_ID) ?: DEFAULT_USER_ID
        loadUser()
        updateStatusText(binding.userStatusSwitch.isChecked)
        binding.userStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateStatusText(isChecked)
        }
        binding.saveUserButton.setOnClickListener {
            if (validateForm()) {
                saveUser()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindUser() {
        val currentUser = user ?: return
        binding.userNameInput.setText(currentUser.name)
        binding.userEmailInput.setText(currentUser.email)
        binding.userRoleInput.setText(roleLabel(currentUser.role))
        binding.userStatusSwitch.isChecked = true
    }

    private fun loadUser() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllUsers()
                .onSuccess { users ->
                    user = users.firstOrNull { it.id.hashCode() == requestedUserId }
                    bindUser()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
            setLoading(false)
        }
    }

    private fun saveUser() {
        val currentUser = user ?: return
        val updatedUser = currentUser.copy(
            name = binding.userNameInput.text?.toString()?.trim().orEmpty(),
            email = binding.userEmailInput.text?.toString()?.trim().orEmpty(),
            role = binding.userRoleInput.text?.toString()?.trim().orEmpty().toFirebaseRole()
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.updateUserProfile(updatedUser)
                .onSuccess {
                    Toast.makeText(requireContext(), R.string.user_updated_success, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.saveUserButton.isEnabled = !isLoading
        binding.userNameInput.isEnabled = !isLoading
        binding.userEmailInput.isEnabled = !isLoading
        binding.userRoleInput.isEnabled = !isLoading
        binding.userStatusSwitch.isEnabled = !isLoading
    }

    private fun validateForm(): Boolean {
        clearErrors()
        var isValid = true
        val name = binding.userNameInput.text?.toString()?.trim().orEmpty()
        val email = binding.userEmailInput.text?.toString()?.trim().orEmpty()
        val role = binding.userRoleInput.text?.toString()?.trim().orEmpty()

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

        if (role.isEmpty()) {
            binding.userRoleLayout.error = getString(R.string.error_user_role_required)
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        binding.userNameLayout.error = null
        binding.userEmailLayout.error = null
        binding.userRoleLayout.error = null
    }

    private fun roleLabel(role: String): String = when (role.lowercase()) {
        "admin" -> getString(R.string.role_admin)
        "seller" -> getString(R.string.role_seller)
        else -> getString(R.string.role_buyer)
    }

    private fun String.toFirebaseRole(): String = when (lowercase()) {
        getString(R.string.role_admin).lowercase(), "admin" -> "admin"
        getString(R.string.role_seller).lowercase(), "seller" -> "seller"
        else -> "buyer"
    }

    private fun updateStatusText(isActive: Boolean) {
        binding.userStatusSwitch.setText(
            if (isActive) R.string.user_status_active else R.string.user_status_inactive
        )
    }

    companion object {
        const val ARG_USER_ID = "userId"
        private const val DEFAULT_USER_ID = 1
    }
}
