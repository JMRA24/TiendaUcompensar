package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.store.R
import com.project.store.databinding.FragmentSalesReportBinding
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class SalesReportFragment : Fragment() {

    private var _binding: FragmentSalesReportBinding? = null
    private val binding get() = _binding!!
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
        val revenue = MockRepository.orders.sumOf { it.total }
        val totalOrders = MockRepository.orders.size
        binding.reportTotalRevenue.text = currencyFormatter.format(revenue)
        binding.reportTotalOrders.text = totalOrders.toString()

        val categoryRevenue = MockRepository.categories.associateWith { category ->
            MockRepository.orders.sumOf { order ->
                order.products.filter { item ->
                    MockRepository.findProductById(item.productId)?.categoryId == category.id
                }.sumOf { it.subtotal }
            }
        }
        val topCategory = categoryRevenue.maxByOrNull { it.value }
        val percent = if (revenue > 0.0 && topCategory != null) {
            (topCategory.value * PERCENT_MAX / revenue).toInt()
        } else {
            PERCENT_EMPTY
        }
        binding.categoryRevenueLabel.text = if (topCategory != null) {
            getString(
                R.string.admin_metric_format,
                topCategory.key.name,
                currencyFormatter.format(topCategory.value)
            )
        } else {
            getString(R.string.report_no_data)
        }
        binding.categoryRevenueBar.progress = percent

        binding.topProducts.text = buildTopProductsText()
    }

    private fun buildTopProductsText(): String {
        val unitsByProduct = MockRepository.orders
            .flatMap { it.products }
            .groupBy { it.productId }
            .mapValues { entry -> entry.value.sumOf { it.quantity } }
            .entries
            .sortedByDescending { it.value }
            .take(TOP_PRODUCTS_LIMIT)

        if (unitsByProduct.isEmpty()) return getString(R.string.report_no_data)

        return unitsByProduct.joinToString(separator = System.lineSeparator()) { entry ->
            val productName = MockRepository.findProductById(entry.key)?.name.orEmpty()
            getString(R.string.report_top_product_line, productName, entry.value)
        }
    }

    companion object {
        private const val PERCENT_MAX = 100
        private const val PERCENT_EMPTY = 0
        private const val TOP_PRODUCTS_LIMIT = 3
    }
}
