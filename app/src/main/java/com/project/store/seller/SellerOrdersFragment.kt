package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.OrderAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentSellerOrdersBinding
import com.project.store.models.Order
import com.project.store.models.OrderItem
import com.project.store.models.OrderStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SellerOrdersFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSellerOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.sellerOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        orderAdapter = OrderAdapter(emptyList()) { order ->
            Toast.makeText(
                requireContext(),
                getString(R.string.label_order_id, order.id.toString()),
                Toast.LENGTH_SHORT
            ).show()
        }
        binding.sellerOrdersRecyclerView.adapter = orderAdapter
        loadOrders()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun loadOrders() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getOrdersBySeller(sellerId)
                .onSuccess { firebaseOrders ->
                    android.util.Log.d(
                        "ORDERS_DEBUG",
                        "Órdenes encontradas: ${firebaseOrders.size}"
                    )
                    firebaseOrders.forEach {
                        android.util.Log.d(
                            "ORDERS_DEBUG",
                            "Orden: ${it.id}, sellerId: ${it.sellerId}, status: ${it.status}"
                        )
                    }
                    val orders = firebaseOrders.map { it.toUiOrder() }
                    binding.sellerOrdersCount.text = getString(R.string.seller_orders_count, orders.size)
                    orderAdapter.submitList(orders)
                }
                .onFailure { error ->
                    android.util.Log.e("ORDERS_DEBUG", "Error: ${error.message}")
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.sellerOrdersRecyclerView.isEnabled = !isLoading
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
