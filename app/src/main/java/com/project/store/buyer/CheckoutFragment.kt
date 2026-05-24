package com.project.store.buyer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.project.store.R
import com.project.store.data.model.Order
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentCheckoutBinding
import com.project.store.ui.payment.PaymentBottomSheet
import com.project.store.utils.CartManager
import com.project.store.utils.LocationHelper
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private var currentLat = 0.0
    private var currentLng = 0.0
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            fetchLocation()
        } else {
            Toast.makeText(
                requireContext(),
                getString(R.string.location_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindBuyerDefaults()
        bindSummary()
        requestLocationOnOpen()
        binding.placeOrderButton.setOnClickListener {
            confirmOrder()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindBuyerDefaults() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCurrentUserFromFirestore()
                .onSuccess { user ->
                    binding.fullNameInput.setText(user.name)
                }
                .onFailure {
                    Toast.makeText(requireContext(), R.string.error_auth, Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun bindSummary() {
        val itemCount = CartManager.getItems().sumOf { it.quantity }
        binding.checkoutItems.text = getString(R.string.checkout_order_preview, itemCount)
        binding.checkoutTotal.text = getString(
            R.string.summary_total,
            currencyFormatter.format(CartManager.getTotal())
        )
    }

    private fun confirmOrder() {
        val buyerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        val address = binding.addressInput.text?.toString()?.trim().orEmpty()

        if (buyerId.isBlank()) {
            Toast.makeText(requireContext(), R.string.error_auth, Toast.LENGTH_SHORT).show()
            return
        }

        if (address.isBlank()) {
            binding.addressLayout.error = getString(R.string.error_address_required)
            return
        }

        val cartItems = CartManager.getItems()
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), R.string.cart_empty_title, Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val firebaseProducts = repository.getProducts().getOrElse { emptyList() }
            val createdAt = System.currentTimeMillis()
            val createdOrderIds = mutableListOf<String>()
            var hasError = false

            for (item in cartItems) {
                val firebaseProduct = firebaseProducts.firstOrNull { it.id.hashCode() == item.productId }
                val order = Order(
                    buyerId = buyerId,
                    sellerId = firebaseProduct?.sellerId.orEmpty(),
                    productId = firebaseProduct?.id ?: item.productId.toString(),
                    quantity = item.quantity,
                    total = item.subtotal,
                    status = "pending",
                    address = address,
                    lat = currentLat,
                    lng = currentLng,
                    createdAt = createdAt
                )
                repository.createOrder(order)
                    .onSuccess { orderId -> createdOrderIds.add(orderId) }
                    .onFailure { hasError = true }
            }
            setLoading(false)

            if (hasError || createdOrderIds.isEmpty()) {
                Toast.makeText(requireContext(), R.string.error_unknown, Toast.LENGTH_SHORT).show()
            } else {
                showPaymentSheet(createdOrderIds.first(), buyerId)
            }
        }
    }

    private fun showPaymentSheet(orderId: String, buyerId: String) {
        val sheet = PaymentBottomSheet.newInstance(
            orderId = orderId,
            amount = CartManager.getTotal(),
            buyerId = buyerId
        )
        sheet.onPaymentResult = { success, transactionId ->
            if (success) {
                CartManager.clear()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.payment_approved) + " - " +
                        getString(R.string.payment_transaction, transactionId),
                    Toast.LENGTH_LONG
                ).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.payment_rejected),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        sheet.show(parentFragmentManager, PaymentBottomSheet.TAG)
    }

    private fun setLoading(isLoading: Boolean) {
        binding.placeOrderButton.isEnabled = !isLoading
        binding.fullNameInput.isEnabled = !isLoading
        binding.addressInput.isEnabled = !isLoading
    }

    private fun requestLocationOnOpen() {
        if (hasLocationPermission()) {
            fetchLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun fetchLocation() {
        viewLifecycleOwner.lifecycleScope.launch {
            val location = LocationHelper.getCurrentLocation(requireContext())
            if (location != null) {
                currentLat = location.first
                currentLng = location.second
                val address = LocationHelper.getAddressFromLatLng(
                    requireContext(),
                    currentLat,
                    currentLng
                )
                binding.addressInput.setText(address)
                Toast.makeText(requireContext(), "📍 $address", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.location_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
}
