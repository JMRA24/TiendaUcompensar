package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.project.store.R
import com.project.store.databinding.FragmentSellerProfileBinding
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class SellerProfileFragment : Fragment() {

    private var _binding: FragmentSellerProfileBinding? = null
    private val binding get() = _binding!!
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
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProfile() {
        val seller = MockRepository.getUsersByRole(UserRole.SELLER).first()
        val products = MockRepository.getProductsBySeller(seller.id)
        val productIds = products.map { it.id }.toSet()
        val totalSales = MockRepository.orders.sumOf { order ->
            order.products.filter { it.productId in productIds }.sumOf { it.subtotal }
        }

        binding.sellerName.text = seller.fullName
        binding.sellerEmail.text = seller.email
        binding.sellerPhone.text = seller.phone
        binding.sellerProductsStat.text = getString(
            R.string.seller_products_count,
            products.size
        )
        binding.sellerSalesStat.text = getString(
            R.string.summary_total,
            currencyFormatter.format(totalSales)
        )
    }
}
