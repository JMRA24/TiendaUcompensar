package com.project.store.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentProductDetailBinding
import com.project.store.models.Product
import com.project.store.utils.CartManager
import com.project.store.utils.loadProductImage
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = arguments?.getInt(HomeFragment.ARG_PRODUCT_ID) ?: DEFAULT_PRODUCT_ID
        loadProduct(productId)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProduct() {
        val currentProduct = product
        if (currentProduct == null) {
            binding.productDetailName.setText(R.string.product_not_found)
            binding.addToCartButton.isEnabled = false
            return
        }

        binding.productDetailImage.loadProductImage(currentProduct.imageName)
        binding.productDetailName.text = currentProduct.name
        binding.productDetailPrice.text = currencyFormatter.format(currentProduct.price)
        binding.productDetailRating.text = getString(
            R.string.product_detail_rating_format,
            getString(R.string.product_rating_value)
        )
        binding.productDetailDescription.text = currentProduct.description
        binding.productDetailStock.text = getString(R.string.product_in_stock, currentProduct.stock)
        binding.addToCartButton.setOnClickListener {
            CartManager.addProduct(currentProduct)
            Toast.makeText(requireContext(), R.string.added_to_cart_success, Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.nav_cart)
        }
    }

    private fun loadProduct(productId: Int) {
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProducts()
                .onSuccess { products ->
                    product = products.firstOrNull { it.id.hashCode() == productId }?.toUiProduct()
                    setLoading(false)
                    bindProduct()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                    setLoading(false)
                    bindProduct()
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.addToCartButton.isEnabled = !isLoading
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

    companion object {
        private const val DEFAULT_PRODUCT_ID = 1
    }
}
