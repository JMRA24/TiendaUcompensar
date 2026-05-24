package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.project.store.R
import com.project.store.data.model.Order
import com.project.store.data.model.Product
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentSalesReportBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindReport()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindReport() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ordersResult = repository.getAllOrders()
            val productsResult = repository.getProducts()

            val orders = ordersResult.getOrElse { error ->
                if (error.message?.contains("UNAVAILABLE") == true ||
                    error.message?.contains("network") == true
                ) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                }
                emptyList()
            }
            val products = productsResult.getOrElse { error ->
                if (error.message?.contains("UNAVAILABLE") == true ||
                    error.message?.contains("network") == true
                ) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                }
                emptyList()
            }

            bindReportData(orders, products)
        }
    }

    private fun bindReportData(orders: List<Order>, products: List<Product>) {
        val revenue = orders.sumOf { it.total }
        val totalOrders = orders.size
        binding.reportTotalRevenue.text = currencyFormatter.format(revenue)
        binding.reportTotalOrders.text = totalOrders.toString()

        val productsById = products.associateBy { it.id }
        val categoryRevenue = orders
            .groupBy { productsById[it.productId]?.category.orEmpty() }
            .filterKeys { it.isNotBlank() }
            .mapValues { entry -> entry.value.sumOf { it.total } }

        val topCategory = categoryRevenue.maxByOrNull { it.value }
        val percent = if (revenue > 0.0 && topCategory != null) {
            (topCategory.value * PERCENT_MAX / revenue).toInt()
        } else {
            PERCENT_EMPTY
        }
        binding.categoryRevenueLabel.text = if (topCategory != null) {
            getString(
                R.string.admin_metric_format,
                topCategory.key,
                currencyFormatter.format(topCategory.value)
            )
        } else {
            getString(R.string.report_no_data)
        }
        binding.categoryRevenueBar.progress = percent

        binding.topProducts.text = buildTopProductsText(orders, productsById)
    }

    private fun buildTopProductsText(
        orders: List<Order>,
        productsById: Map<String, Product>
    ): String {
        val unitsByProduct = orders
            .groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .entries
            .sortedByDescending { it.value }
            .take(TOP_PRODUCTS_LIMIT)

        if (unitsByProduct.isEmpty()) return getString(R.string.report_no_data)

        return unitsByProduct.joinToString(separator = System.lineSeparator()) { entry ->
            val productName = productsById[entry.key]?.name.orEmpty()
            getString(R.string.report_top_product_line, productName, entry.value)
        }
    }

    companion object {
        private const val PERCENT_MAX = 100
        private const val PERCENT_EMPTY = 0
        private const val TOP_PRODUCTS_LIMIT = 3
    }
}
