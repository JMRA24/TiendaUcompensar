package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.project.store.R
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentAdminDashboardBinding
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()

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
        viewLifecycleOwner.lifecycleScope.launch {
            val usersResult = repository.getAllUsers()
            val ordersResult = repository.getAllOrders()
            val productsResult = repository.getProducts()

            usersResult.onSuccess { users ->
                binding.totalUsers.text = users.size.toString()
                bindSimpleCharts(users.count { it.role == ROLE_BUYER }, users.size)
            }.onFailure { error ->
                if (error.message?.contains("UNAVAILABLE") == true ||
                    error.message?.contains("network") == true
                ) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            }

            productsResult.onSuccess { products ->
                binding.totalProducts.text = products.count { it.stock > 0 }.toString()
            }.onFailure { error ->
                if (error.message?.contains("UNAVAILABLE") == true ||
                    error.message?.contains("network") == true
                ) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            }

            ordersResult.onSuccess { orders ->
                binding.totalOrders.text = orders.size.toString()
                binding.monthlyRevenue.text = formatCompactCurrency(orders.sumOf { it.total })
            }.onFailure { error ->
                if (error.message?.contains("UNAVAILABLE") == true ||
                    error.message?.contains("network") == true
                ) {
                    Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bindSimpleCharts(buyers: Int = 0, usersCount: Int = 0) {
        val totalUsers = usersCount.coerceAtLeast(1)
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
        private const val ROLE_BUYER = "buyer"
        private const val PERCENT_MAX = 100
        private const val THOUSAND = 1_000.0
        private const val MILLION = 1_000_000.0
    }
}
