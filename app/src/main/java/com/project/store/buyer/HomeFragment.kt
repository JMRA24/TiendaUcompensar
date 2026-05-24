package com.project.store.buyer

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.CategoryAdapter
import com.project.store.adapters.ProductAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentHomeBinding
import com.project.store.models.Product
import com.project.store.utils.SecurePreferences
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var productAdapter: ProductAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private var products: List<Product> = emptyList()
    private var categoryNamesById: Map<Int, String> = emptyMap()

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
        setupBanners()
        setupFeaturedProducts()
        setupCategories()
        setupSearch()
        setupGreeting()
        loadHomeData()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupGreeting() {
        val localName = SecurePreferences.getUserName(requireContext())
        if (!localName.isNullOrEmpty()) {
            binding.homeGreeting.text = getString(R.string.home_greeting, localName)
        }

        val firebaseUser = repository.getCurrentFirebaseUser()
        if (firebaseUser != null) {
            viewLifecycleOwner.lifecycleScope.launch {
                repository.getCurrentUserFromFirestore()
                    .onSuccess { user ->
                        binding.homeGreeting.text = getString(R.string.home_greeting, user.name)
                        SecurePreferences.saveUserSession(requireContext(), user.id, user.role, user.name)
                    }
                    .onFailure {
                        if (localName.isNullOrEmpty()) {
                            val name = firebaseUser.displayName ?: firebaseUser.email ?: "explorador"
                            binding.homeGreeting.text = getString(R.string.home_greeting, name)
                        }
                    }
            }
        } else if (localName.isNullOrEmpty()) {
            binding.homeGreeting.text = getString(R.string.home_greeting_guest)
        }
    }

    private fun setupBanners() {
        binding.bannerFlipper.startFlipping()
    }

    private fun setupCategories() {
        categoryAdapter = CategoryAdapter(listOf(allCategory())) { category ->
            categoryAdapter.setSelectedCategory(category.id)
            val filteredProducts = category.id?.let { categoryId ->
                products.filter { it.categoryId == categoryId }
            } ?: products.take(FEATURED_PRODUCTS_COUNT)
            productAdapter.submitList(filteredProducts)
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
        productAdapter = ProductAdapter(emptyList()) {
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
                val filteredProducts = products.filter { product ->
                    query.isBlank() ||
                        product.name.contains(query, ignoreCase = true) ||
                        product.description.contains(query, ignoreCase = true)
                }
                productAdapter.submitList(filteredProducts)
            }

            override fun afterTextChanged(editable: Editable?) = Unit
        })
    }

    private fun loadHomeData() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { firebaseProducts ->
                    categoryNamesById = firebaseProducts
                        .map { it.category.hashCode() to it.category }
                        .distinctBy { it.first }
                        .toMap()
                    products = firebaseProducts.map { it.toUiProduct() }
                    productAdapter.submitList(products.take(FEATURED_PRODUCTS_COUNT))
                    categoryAdapter.submitList(
                        listOf(allCategory()) + categoryNamesById.map { (id, name) ->
                            CategoryAdapter.CategoryItem(id = id, name = name)
                        }
                    )
                }
                .onFailure {
                    if (it.message?.contains("UNAVAILABLE") == true ||
                        it.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
            setLoading(false)
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.homeSearchInput.isEnabled = !isLoading
    }

    private fun allCategory(): CategoryAdapter.CategoryItem {
        return CategoryAdapter.CategoryItem(id = null, name = getString(R.string.category_all))
    }

    private fun com.project.store.data.model.Product.toUiProduct(): Product {
        return Product(
            id = id.hashCode(),
            name = name,
            description = description,
            price = price,
            stock = stock,
            categoryId = category.hashCode(),
            sellerId = sellerId.hashCode(),
            imageName = imageUrl,
            isAvailable = stock > 0
        )
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
