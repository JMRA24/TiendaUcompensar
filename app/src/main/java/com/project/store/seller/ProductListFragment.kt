package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.ProductAdapter
import com.project.store.databinding.FragmentProductListBinding
import com.project.store.models.Product
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupProducts()
        binding.addProductFab.setOnClickListener {
            findNavController().navigate(R.id.nav_create_product)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupProducts() {
        val seller = MockRepository.getUsersByRole(UserRole.SELLER).first()
        val products = MockRepository.getProductsBySeller(seller.id)
        binding.productsCount.text = getString(R.string.seller_products_count, products.size)
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.productsRecyclerView.adapter = ProductAdapter(products) { openEditProduct(it) }
    }

    private fun openEditProduct(product: Product) {
        val args = Bundle().apply {
            putInt(EditProductFragment.ARG_PRODUCT_ID, product.id)
        }
        findNavController().navigate(R.id.nav_edit_product, args)
    }
}
