package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.project.store.R
import com.project.store.adapters.UserAdapter
import com.project.store.databinding.FragmentUserListBinding
import com.project.store.models.User
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class UserListFragment : Fragment() {

    private var _binding: FragmentUserListBinding? = null
    private val binding get() = _binding!!
    private lateinit var userAdapter: UserAdapter
    private var selectedRole: UserRole? = null

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
        applyFilter()
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
        val users = selectedRole?.let { MockRepository.getUsersByRole(it) } ?: MockRepository.users
        userAdapter.submitList(users)
        binding.usersCount.text = getString(R.string.admin_users_count, users.size)
    }

    private fun openEditUser(user: User) {
        val args = Bundle().apply {
            putInt(EditUserFragment.ARG_USER_ID, user.id)
        }
        findNavController().navigate(R.id.nav_edit_user, args)
    }
}
