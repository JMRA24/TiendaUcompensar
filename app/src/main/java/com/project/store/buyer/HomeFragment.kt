package com.project.store.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.CategoryAdapter
import com.project.store.adapters.ProductAdapter
import com.project.store.databinding.FragmentHomeBinding
import com.project.store.models.Product
import com.project.store.models.UserRole
import com.project.store.utils.MockRepository

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupGreeting()
        setupBanners()
        setupCategories()
        setupFeaturedProducts()
        setupSearch()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupGreeting() {
        val buyer = MockRepository.getUsersByRole(UserRole.BUYER).firstOrNull()
        binding.homeGreeting.text = if (buyer != null) {
            getString(R.string.home_greeting, buyer.fullName)
        } else {
            getString(R.string.home_greeting_guest)
        }
    }

    private fun setupBanners() {
        binding.bannerFlipper.startFlipping()
    }

    private fun setupCategories() {
        val categories = listOf(
            CategoryAdapter.CategoryItem(id = null, name = getString(R.string.category_all))
        ) + CategoryAdapter.CategoryItem.fromMockCategories()

        categoryAdapter = CategoryAdapter(categories) { category ->
            categoryAdapter.setSelectedCategory(category.id)
            val products = category.id?.let { MockRepository.getProductsByCategory(it) }
                ?: MockRepository.products.take(FEATURED_PRODUCTS_COUNT)
            productAdapter.submitList(products)
        }
        categoryAdapter.setSelectedCategory(null)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupFeaturedProducts() {
        productAdapter = ProductAdapter(MockRepository.products.take(FEATURED_PRODUCTS_COUNT)) {
            openProductDetail(it)
        }
        binding.featuredProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.featuredProductsRecyclerView.adapter = productAdapter
    }

    private fun setupSearch() {
        binding.homeSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val query = text?.toString().orEmpty()
                val products = MockRepository.products.filter { product ->
                    query.isBlank() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
                }
                productAdapter.submitList(products)
            }

            override fun afterTextChanged(editable: Editable?) = Unit
        })
    }

    private fun openProductDetail(product: Product) {
        val args = Bundle().apply {
            putInt(ARG_PRODUCT_ID, product.id)
        }
        findNavController().navigate(
            R.id.nav_product_detail,
            args
        )
    }

    companion object {
        private const val FEATURED_PRODUCTS_COUNT = 6
        const val ARG_PRODUCT_ID = "productId"
    }
}
