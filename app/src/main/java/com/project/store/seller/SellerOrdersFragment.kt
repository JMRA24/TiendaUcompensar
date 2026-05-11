package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.OrderAdapter
import com.project.store.databinding.FragmentSellerOrdersBinding
import com.project.store.models.Order
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class SellerOrdersFragment : Fragment() {

    private var _binding: FragmentSellerOrdersBinding? = null
    private val binding get() = _binding!!

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
        val orders = getSellerOrders()
        binding.sellerOrdersCount.text = getString(R.string.seller_orders_count, orders.size)
        binding.sellerOrdersRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.sellerOrdersRecyclerView.adapter = OrderAdapter(orders) { order ->
            Toast.makeText(
                requireContext(),
                getString(R.string.label_order_id, order.id.toString()),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun getSellerOrders(): List<Order> {
        val seller = MockRepository.getUsersByRole(UserRole.SELLER).first()
        val productIds = MockRepository.getProductsBySeller(seller.id).map { it.id }.toSet()
        return MockRepository.orders.filter { order ->
            order.products.any { it.productId in productIds }
        }
    }
}
