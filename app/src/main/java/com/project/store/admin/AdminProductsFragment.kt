package com.project.store.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.ProductAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentAdminProductsBinding
import com.project.store.models.Product
import kotlinx.coroutines.launch

class AdminProductsFragment : Fragment() {

    private var _binding: FragmentAdminProductsBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var productAdapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.adminProductsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { product ->
                Toast.makeText(
                    requireContext(),
                    getString(R.string.admin_product_selected, product.name),
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDeleteClick = { product ->
                androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar producto")
                    .setMessage("¿Eliminar \"${product.name}\" del catálogo?")
                    .setPositiveButton("Eliminar") { _, _ ->
                        deleteProduct(product)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            },
            showDeleteButton = true
        )
        binding.adminProductsRecyclerView.adapter = productAdapter
        loadProducts()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun loadProducts() {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { firebaseProducts ->
                    val products = firebaseProducts.map { it.toUiProduct() }
                    binding.adminProductsCount.text = getString(
                        R.string.admin_products_count,
                        products.size
                    )
                    productAdapter.submitList(products)
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

    private fun setLoading(isLoading: Boolean) {
        binding.adminProductsRecyclerView.isEnabled = !isLoading
    }

    private fun deleteProduct(product: Product) {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { firebaseProducts ->
                    val firebaseProduct = firebaseProducts.firstOrNull {
                        it.name == product.name && it.price == product.price
                    }
                    if (firebaseProduct != null) {
                        repository.deleteProduct(firebaseProduct.id)
                            .onSuccess {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.success_deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadProducts()
                            }
                    }
                }
        }
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
}
