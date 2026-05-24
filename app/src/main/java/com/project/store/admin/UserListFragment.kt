package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.project.store.R
import com.project.store.adapters.UserAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentUserListBinding
import com.project.store.models.User
import com.project.store.models.UserRole
import kotlinx.coroutines.launch

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var userAdapter: UserAdapter
    private var selectedRole: UserRole? = null
    private var users: List<User> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRoleFilters()
        binding.addUserFab.setOnClickListener {
            findNavController().navigate(R.id.nav_create_user)
        }
        loadUsers()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter(emptyList()) { openEditUser(it) }
        binding.usersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.usersRecyclerView.adapter = userAdapter
    }

    private fun setupRoleFilters() {
        val allChip = createRoleChip(getString(R.string.filter_role_all), null)
        allChip.isChecked = true
        binding.roleChipGroup.addView(allChip)
        binding.roleChipGroup.addView(createRoleChip(getString(R.string.role_admin), UserRole.ADMIN))
        binding.roleChipGroup.addView(createRoleChip(getString(R.string.role_seller), UserRole.SELLER))
        binding.roleChipGroup.addView(createRoleChip(getString(R.string.role_buyer), UserRole.BUYER))
    }

    private fun createRoleChip(label: String, role: UserRole?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            setOnClickListener {
                selectedRole = role
                applyFilter()
            }
        }
    }

    private fun applyFilter() {
        val filteredUsers = selectedRole?.let { role ->
            users.filter { it.role == role }
        } ?: users
        userAdapter.submitList(filteredUsers)
        binding.usersCount.text = getString(R.string.admin_users_count, filteredUsers.size)
    }

    private fun loadUsers() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getAllUsers()
                .onSuccess { firebaseUsers ->
                    users = firebaseUsers.map { it.toUiUser() }
                    applyFilter()
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
        binding.addUserFab.isEnabled = !isLoading
        binding.roleChipGroup.isEnabled = !isLoading
    }

    private fun com.project.store.data.model.User.toUiUser(): User {
        return User(
            id = id.hashCode(),
            fullName = name,
            email = email,
            password = "",
            role = role.toUiRole(),
            phone = phone,
            isActive = true
        )
    }

    private fun String.toUiRole(): UserRole {
        return when (lowercase()) {
            "admin" -> UserRole.ADMIN
            "seller" -> UserRole.SELLER
            else -> UserRole.BUYER
        }
    }

    private fun openEditUser(user: User) {
        val args = Bundle().apply {
            putInt(EditUserFragment.ARG_USER_ID, user.id)
        }
        findNavController().navigate(R.id.nav_edit_user, args)
    }
}
