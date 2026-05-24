package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentSellerDashboardBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class SellerDashboardFragment : Fragment() {

    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindDashboard()
        setupQuickActions()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindDashboard() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getCurrentUserFromFirestore()
                .onSuccess { user ->
                    binding.sellerWelcome.text = getString(R.string.seller_dashboard_welcome, user.name)
                }
                .onFailure {
                    binding.sellerWelcome.text = getString(R.string.seller_dashboard_welcome, "")
                }

            val productsResult = repository.getProductsBySeller(sellerId)
            val ordersResult = repository.getOrdersBySeller(sellerId)

            productsResult
                .onSuccess { products ->
                    binding.activeProducts.text = products.count { it.stock > 0 }.toString()
                    binding.lowStockProducts.text = products.count { it.stock <= LOW_STOCK_LIMIT }.toString()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }

            ordersResult
                .onSuccess { orders ->
                    val activeOrders = orders.filterNot { it.status.equals("cancelled", ignoreCase = true) }
                    binding.totalRevenue.text = formatCompactCurrency(activeOrders.sumOf { it.total })
                    binding.totalOrders.text = activeOrders.size.toString()
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

    private fun setupQuickActions() {
        binding.openProductsButton.setOnClickListener {
            findNavController().navigate(R.id.nav_seller_products)
        }
        binding.createProductButton.setOnClickListener {
            val args = Bundle().apply {
                putInt(EditProductFragment.ARG_PRODUCT_ID, NEW_PRODUCT_ID)
            }
            findNavController().navigate(R.id.nav_edit_product, args)
        }
        binding.openOrdersButton.setOnClickListener {
            findNavController().navigate(R.id.nav_seller_orders)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.openProductsButton.isEnabled = !isLoading
        binding.createProductButton.isEnabled = !isLoading
        binding.openOrdersButton.isEnabled = !isLoading
    }

    private fun formatCompactCurrency(value: Double): String = when {
        value >= MILLION -> getString(
            R.string.format_currency_millions,
            value / MILLION
        )
        value >= THOUSAND -> getString(
            R.string.format_currency_thousands,
            (value / THOUSAND).roundToInt()
        )
        else -> getString(R.string.format_price, value.roundToInt().toString())
    }

    companion object {
        private const val NEW_PRODUCT_ID = 0
        private const val LOW_STOCK_LIMIT = 10
        private const val THOUSAND = 1_000.0
        private const val MILLION = 1_000_000.0
    }
}
