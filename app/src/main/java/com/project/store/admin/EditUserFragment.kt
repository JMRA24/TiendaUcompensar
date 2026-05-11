package com.project.store.admin

import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.databinding.FragmentEditUserBinding
import com.project.store.models.User
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class EditUserFragment : Fragment() {

    private var _binding: FragmentEditUserBinding? = null
    private val binding get() = _binding!!
    private var user: User? = null

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
        val userId = arguments?.getInt(ARG_USER_ID) ?: DEFAULT_USER_ID
        user = MockRepository.users.firstOrNull { it.id == userId }
        bindUser()
        updateStatusText(binding.userStatusSwitch.isChecked)
        binding.userStatusSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateStatusText(isChecked)
        }
        binding.saveUserButton.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(requireContext(), R.string.user_updated_success, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindUser() {
        val currentUser = user ?: return
        binding.userNameInput.setText(currentUser.fullName)
        binding.userEmailInput.setText(currentUser.email)
        binding.userRoleInput.setText(roleLabel(currentUser.role))
        binding.userStatusSwitch.isChecked = currentUser.isActive
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

    private fun roleLabel(role: UserRole): String = when (role) {
        UserRole.ADMIN -> getString(R.string.role_admin)
        UserRole.SELLER -> getString(R.string.role_seller)
        UserRole.BUYER -> getString(R.string.role_buyer)
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
