package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.databinding.FragmentSellerDashboardBinding
import com.project.store.models.Order
import com.project.store.models.OrderStatus
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository
import kotlin.math.roundToInt

class SellerDashboardFragment : Fragment() {

    private var _binding: FragmentSellerDashboardBinding? = null
    private val binding get() = _binding!!

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
        val seller = MockRepository.getUsersByRole(UserRole.SELLER).first()
        val products = MockRepository.getProductsBySeller(seller.id)
        val productIds = products.map { it.id }.toSet()
        val orders = getSellerOrders(productIds)
        val revenue = orders.sumOf { order ->
            order.products.filter { it.productId in productIds }.sumOf { it.subtotal }
        }

        binding.sellerWelcome.text = getString(R.string.seller_dashboard_welcome, seller.fullName)
        binding.totalRevenue.text = formatCompactCurrency(revenue)
        binding.totalOrders.text = orders.size.toString()
        binding.activeProducts.text = products.count { it.isAvailable }.toString()
        binding.lowStockProducts.text = products.count { it.stock <= LOW_STOCK_LIMIT }.toString()
    }

    private fun setupQuickActions() {
        binding.openProductsButton.setOnClickListener {
            findNavController().navigate(R.id.nav_seller_products)
        }
        binding.createProductButton.setOnClickListener {
            findNavController().navigate(R.id.nav_create_product)
        }
        binding.openOrdersButton.setOnClickListener {
            findNavController().navigate(R.id.nav_seller_orders)
        }
    }

    private fun getSellerOrders(productIds: Set<Int>): List<Order> {
        return MockRepository.orders.filter { order ->
            order.status != OrderStatus.CANCELLED &&
            order.products.any { it.productId in productIds }
        }
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
        private const val LOW_STOCK_LIMIT = 10
        private const val THOUSAND = 1_000.0
        private const val MILLION = 1_000_000.0
    }
}
