package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.project.store.R
import androidx.lifecycle.lifecycleScope
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentSellerProfileBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class SellerProfileFragment : Fragment() {

    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindProfile()
        binding.editSellerProfileButton.setOnClickListener {
            showEditDialog()
        }
        binding.sellerLogoutButton.setOnClickListener {
            logout()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProfile() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCurrentUserFromFirestore()
                .onSuccess { seller ->
                    binding.sellerName.text = seller.name
                    binding.sellerEmail.text = seller.email
                    binding.sellerPhone.text = seller.phone
                }
                .onFailure {
                    val firebaseUser = repository.getCurrentFirebaseUser()
                    if (firebaseUser != null) {
                        binding.sellerName.text = firebaseUser.displayName ?: firebaseUser.email.orEmpty()
                        binding.sellerEmail.text = firebaseUser.email.orEmpty()
                        binding.sellerPhone.text = ""
                    }
                }

            repository.getProductsBySeller(sellerId)
                .onSuccess { products ->
                    binding.sellerProductsStat.text = getString(
                        R.string.seller_products_count,
                        products.size
                    )
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }

            repository.getOrdersBySeller(sellerId)
                .onSuccess { orders ->
                    binding.sellerSalesStat.text = getString(
                        R.string.summary_total,
                        currencyFormatter.format(orders.sumOf { it.total })
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
    }

    private fun showEditDialog() {
        val layout = android.widget.LinearLayout(requireContext()).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(64, 32, 64, 32)
        }
        val nameInput = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            setText(binding.sellerName.text)
            hint = getString(R.string.profile_name_hint)
        }
        val phoneInput = com.google.android.material.textfield.TextInputEditText(requireContext()).apply {
            setText(binding.sellerPhone.text)
            hint = getString(R.string.profile_phone_hint)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        layout.addView(nameInput)
        layout.addView(phoneInput)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.profile_edit_title))
            .setView(layout)
            .setPositiveButton(getString(R.string.btn_save)) { _, _ ->
                val newName = nameInput.text?.toString()?.trim().orEmpty()
                val newPhone = phoneInput.text?.toString()?.trim().orEmpty()
                if (newName.isNotEmpty()) saveProfile(newName, newPhone)
            }
            .setNegativeButton(getString(R.string.btn_cancel), null)
            .show()
    }

    private fun saveProfile(name: String, phone: String) {
        val uid = repository.getCurrentFirebaseUser()?.uid ?: return
        viewLifecycleOwner.lifecycleScope.launch {
            val user = com.project.store.data.model.User(
                id = uid,
                email = repository.getCurrentFirebaseUser()?.email ?: "",
                name = name,
                phone = phone,
                role = "seller"
            )
            repository.updateUserProfile(user)
                .onSuccess {
                    binding.sellerName.text = name
                    binding.sellerPhone.text = phone
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

    private fun logout() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.logout(requireContext())
            val intent = android.content.Intent(
                requireContext(),
                com.project.store.auth.LoginActivity::class.java
            ).apply {
                flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                    android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }
}
