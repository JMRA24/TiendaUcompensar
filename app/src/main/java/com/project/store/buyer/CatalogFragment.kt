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
import com.google.android.material.chip.Chip
import com.project.store.R
import com.project.store.adapters.ProductAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentCatalogBinding
import com.project.store.models.Product
import kotlinx.coroutines.launch

class CatalogFragment : Fragment() {

    private var _binding: FragmentCatalogBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var productAdapter: ProductAdapter
    private var selectedCategoryId: Int? = null
    private var query: String = ""
    private var products: List<Product> = emptyList()
    private var categoryNamesById: Map<Int, String> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCatalogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearch()
        loadProducts()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(emptyList()) { openProductDetail(it) }
        binding.catalogRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.catalogRecyclerView.adapter = productAdapter
    }

    private fun setupFilters() {
        binding.filterChipGroup.removeAllViews()
        val allChip = createFilterChip(getString(R.string.filter_all_categories), null)
        allChip.isChecked = true
        binding.filterChipGroup.addView(allChip)

        categoryNamesById.map { it.key to it.value }
            .distinctBy { it.first }
            .forEach { (categoryId, categoryName) ->
                binding.filterChipGroup.addView(createFilterChip(categoryName, categoryId))
            }
    }

    private fun loadProducts() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { firebaseProducts ->
                    categoryNamesById = firebaseProducts
                        .map { it.category.hashCode() to it.category }
                        .distinctBy { it.first }
                        .toMap()
                    products = firebaseProducts.map { firebaseProduct ->
                        firebaseProduct.toUiProduct()
                    }
                    setupFilters()
                    applyFilters()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
            setLoading(false)
        }
    }

    private fun setupSearch() {
        binding.catalogSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                query = text?.toString().orEmpty()
                applyFilters()
            }

            override fun afterTextChanged(editable: Editable?) = Unit
        })
    }

    private fun createFilterChip(label: String, categoryId: Int?): Chip {
        return Chip(requireContext()).apply {
            text = label
            isCheckable = true
            setOnClickListener {
                selectedCategoryId = categoryId
                applyFilters()
            }
        }
    }

    private fun applyFilters() {
        val filteredProducts = products.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesQuery = query.isBlank() ||
                product.name.contains(query, ignoreCase = true) ||
                product.description.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery && product.isAvailable
        }

        productAdapter.submitList(filteredProducts)
        binding.catalogResultsCount.text = resources.getQuantityString(
            R.plurals.catalog_results_quantity,
            filteredProducts.size,
            filteredProducts.size
        )
    }

    private fun setLoading(isLoading: Boolean) {
        binding.catalogSearchInput.isEnabled = !isLoading
        binding.filterChipGroup.isEnabled = !isLoading
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
            putInt(HomeFragment.ARG_PRODUCT_ID, product.id)
        }
        findNavController().navigate(
            R.id.nav_product_detail,
            args
        )
    }
}
