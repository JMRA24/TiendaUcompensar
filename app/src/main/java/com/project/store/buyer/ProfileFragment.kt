package com.project.store.buyer

import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.project.store.R
import com.project.store.adapters.OrderHistoryAdapter
import com.project.store.auth.LoginActivity
import com.project.store.data.model.User
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentProfileBinding
import com.project.store.models.Order
import com.project.store.models.OrderItem
import com.project.store.models.OrderStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindProfile()
        binding.logoutButton.setOnClickListener {
            logout()
        }
        binding.editProfileButton.setOnClickListener {
            showEditDialog()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProfile() {
        binding.orderHistoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.orderHistoryRecyclerView.adapter = OrderHistoryAdapter(emptyList())

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCurrentUserFromFirestore()
                .onSuccess { user ->
                    binding.profileName.text = user.name
                    binding.profileEmail.text = user.email
                    binding.profilePhone.text = user.phone

                    repository.getOrdersByBuyer(user.id)
                        .onSuccess { orders ->
                            binding.orderHistoryRecyclerView.adapter = OrderHistoryAdapter(
                                orders.map { it.toUiOrder() }
                            )
                        }
                        .onFailure { error ->
                            if (error.message?.contains("UNAVAILABLE") == true ||
                                error.message?.contains("network") == true
                            ) {
                                Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                .onFailure {
                    val firebaseUser = repository.getCurrentFirebaseUser()
                    if (firebaseUser != null) {
                        binding.profileName.text = firebaseUser.displayName ?: firebaseUser.email.orEmpty()
                        binding.profileEmail.text = firebaseUser.email.orEmpty()
                        binding.profilePhone.text = ""
                    }
                }
            setLoading(false)
        }
    }

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.logout(requireContext())
            val intent = android.content.Intent(requireContext(), LoginActivity::class.java).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun showEditDialog() {
        val firebaseUser = repository.getCurrentFirebaseUser() ?: return

        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 32)
        }

        val nameInput = TextInputEditText(requireContext()).apply {
            setText(binding.profileName.text)
            hint = getString(R.string.profile_name_hint)
        }

        val phoneInput = TextInputEditText(requireContext()).apply {
            setText(binding.profilePhone.text)
            hint = getString(R.string.profile_phone_hint)
            inputType = InputType.TYPE_CLASS_PHONE
        }

        layout.addView(nameInput)
        layout.addView(phoneInput)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.profile_edit_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newName = nameInput.text?.toString()?.trim().orEmpty()
                val newPhone = phoneInput.text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) {
                    saveProfile(firebaseUser.uid, newName, newPhone)
                }
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun saveProfile(uid: String, name: String, phone: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            val currentUser = User(
                id = uid,
                email = repository.getCurrentFirebaseUser()?.email ?: "",
                name = name,
                phone = phone,
                role = "buyer"
            )
            repository.updateUserProfile(currentUser)
                .onSuccess {
                    binding.profileName.text = name
                    binding.profilePhone.text = phone
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.profile_save_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .onFailure {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.error_unknown),
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.logoutButton.isEnabled = !isLoading
        binding.editProfileButton.isEnabled = !isLoading
    }

    private fun com.project.store.data.model.Order.toUiOrder(): Order {
        return Order(
            id = id.hashCode(),
            buyerId = buyerId.hashCode(),
            products = listOf(
                OrderItem(
                    productId = productId.hashCode(),
                    quantity = quantity,
                    unitPrice = if (quantity > 0) total / quantity else total
                )
            ),
            status = status.toUiOrderStatus(),
            orderDate = createdAt.toOrderDate(),
            shippingAddress = address
        )
    }

    private fun String.toUiOrderStatus(): OrderStatus {
        return when (lowercase()) {
            "processing", "paid" -> OrderStatus.PROCESSING
            "shipped" -> OrderStatus.SHIPPED
            "delivered" -> OrderStatus.DELIVERED
            "cancelled", "rejected" -> OrderStatus.CANCELLED
            else -> OrderStatus.PENDING
        }
    }

    private fun Long.toOrderDate(): String {
        if (this <= 0L) return ""
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(this))
    }
}
