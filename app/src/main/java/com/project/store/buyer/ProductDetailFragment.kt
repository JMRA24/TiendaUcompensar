package com.project.store.buyer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.databinding.FragmentProductDetailBinding
import com.project.store.models.Product
import com.project.store.utils.CartManager
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
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
        product = MockRepository.findProductById(productId)
        bindProduct()
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

        binding.productDetailImage.setImageResource(R.drawable.ic_placeholder_product)
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

    companion object {
        private const val DEFAULT_PRODUCT_ID = 1
    }
}
