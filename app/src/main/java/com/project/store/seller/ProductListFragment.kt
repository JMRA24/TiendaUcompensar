package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.project.store.R
import com.project.store.adapters.ProductAdapter
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentProductListBinding
import com.project.store.models.Product
import kotlinx.coroutines.launch

class ProductListFragment : Fragment() {

    private var _binding: FragmentProductListBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private lateinit var productAdapter: ProductAdapter

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
            openNewProduct()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupProducts() {
        binding.productsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = ProductAdapter(
            products = emptyList(),
            onProductClick = { openEditProduct(it) },
            onDeleteClick = { product -> confirmDelete(product) },
            showDeleteButton = true
        )
        binding.productsRecyclerView.adapter = productAdapter
        loadProducts()
    }

    private fun loadProducts() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProductsBySeller(sellerId)
                .onSuccess { firebaseProducts ->
                    val products = firebaseProducts.map { it.toUiProduct() }
                    binding.productsCount.text = getString(R.string.seller_products_count, products.size)
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
        binding.addProductFab.isEnabled = !isLoading
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

    private fun openEditProduct(product: Product) {
        val args = Bundle().apply {
            putInt(EditProductFragment.ARG_PRODUCT_ID, product.id)
        }
        findNavController().navigate(R.id.nav_edit_product, args)
    }

    private fun openNewProduct() {
        val args = Bundle().apply {
            putInt(EditProductFragment.ARG_PRODUCT_ID, NEW_PRODUCT_ID)
        }
        findNavController().navigate(R.id.nav_edit_product, args)
    }

    private fun confirmDelete(product: Product) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de que deseas eliminar \"${product.name}\"?")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct(product: Product) {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProductsBySeller(sellerId)
                .onSuccess { firebaseProducts ->
                    android.util.Log.d(
                        "DELETE_DEBUG",
                        "Productos encontrados: ${firebaseProducts.size}"
                    )
                    val firebaseProduct = firebaseProducts.firstOrNull {
                        it.name == product.name && it.price == product.price
                    }
                    android.util.Log.d(
                        "DELETE_DEBUG",
                        "Producto a eliminar: ${firebaseProduct?.id}"
                    )
                    if (firebaseProduct != null) {
                        repository.deleteProduct(firebaseProduct.id)
                            .onSuccess {
                                android.util.Log.d("DELETE_DEBUG", "Eliminado OK")
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.success_deleted),
                                    Toast.LENGTH_SHORT
                                ).show()
                                loadProducts()
                            }
                            .onFailure { error ->
                                android.util.Log.e(
                                    "DELETE_DEBUG",
                                    "Error al eliminar: ${error.message}"
                                )
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.error_unknown),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    } else {
                        android.util.Log.e("DELETE_DEBUG", "Producto no encontrado en Firebase")
                        Toast.makeText(
                            requireContext(),
                            "Producto no encontrado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .onFailure { error ->
                    android.util.Log.e(
                        "DELETE_DEBUG",
                        "Error obteniendo productos: ${error.message}"
                    )
                }
        }
    }

    companion object {
        private const val NEW_PRODUCT_ID = 0
    }
}
