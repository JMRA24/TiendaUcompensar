package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.R
import com.project.store.databinding.ItemUserBinding
import com.project.store.models.User
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class UserAdapter(
    private var users: List<User> = MockRepository.users,
    private val onUserClick: (User) -> Unit = {}
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount(): Int = users.size

    fun submitList(newUsers: List<User>) {
        users = newUsers
        notifyDataSetChanged()
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClick: (User) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            val context = binding.root.context
            binding.userName.text = user.fullName
            binding.userEmail.text = user.email
            binding.userRole.text = context.getString(R.string.user_role_format, user.role.label(context))
            binding.userPhone.text = context.getString(R.string.user_phone_format, user.phone)
            binding.userStatus.text = context.getString(
                if (user.isActive) R.string.user_status_active else R.string.user_status_inactive
            )
            binding.root.setOnClickListener { onUserClick(user) }
        }

        private fun UserRole.label(context: android.content.Context): String = when (this) {
            UserRole.ADMIN -> context.getString(R.string.role_admin)
            UserRole.SELLER -> context.getString(R.string.role_seller)
            UserRole.BUYER -> context.getString(R.string.role_buyer)
        }
    }
}
