package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.store.R
import com.project.store.databinding.FragmentAdminDashboardBinding
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository
import kotlin.math.roundToInt

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindMetrics()
        bindSimpleCharts()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindMetrics() {
        val revenue = MockRepository.orders.sumOf { it.total }
        binding.totalUsers.text = MockRepository.users.size.toString()
        binding.totalProducts.text = MockRepository.products.size.toString()
        binding.totalOrders.text = MockRepository.orders.size.toString()
        binding.monthlyRevenue.text = formatCompactCurrency(revenue)
    }

    private fun bindSimpleCharts() {
        val totalUsers = MockRepository.users.size.coerceAtLeast(1)
        val buyers = MockRepository.getUsersByRole(UserRole.BUYER).size
        val buyerPercent = buyers * PERCENT_MAX / totalUsers
        binding.adminUsersBarLabel.text = getString(
            R.string.admin_metric_format,
            getString(R.string.role_buyer),
            getString(R.string.format_percentage, buyerPercent)
        )
        binding.adminUsersBar.progress = buyerPercent
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
        private const val PERCENT_MAX = 100
        private const val THOUSAND = 1_000.0
        private const val MILLION = 1_000_000.0
    }
}
