package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.databinding.FragmentEditProductBinding
import com.project.store.models.Product
import com.project.store.utils.MockRepository

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private var product: Product? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val productId = arguments?.getInt(ARG_PRODUCT_ID) ?: DEFAULT_PRODUCT_ID
        product = MockRepository.findProductById(productId)
        bindProduct()
        binding.saveProductButton.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(requireContext(), R.string.product_updated_success, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProduct() {
        val currentProduct = product ?: return
        val categoryName = MockRepository.categories
            .firstOrNull { it.id == currentProduct.categoryId }
            ?.name
            .orEmpty()

        binding.productNameInput.setText(currentProduct.name)
        binding.productPriceInput.setText(currentProduct.price.toInt().toString())
        binding.productDescriptionInput.setText(currentProduct.description)
        binding.productCategoryInput.setText(categoryName)
        binding.productStockInput.setText(currentProduct.stock.toString())
    }

    private fun validateForm(): Boolean {
        clearErrors()
        var isValid = true

        val name = binding.productNameInput.text?.toString()?.trim().orEmpty()
        val price = binding.productPriceInput.text?.toString()?.toDoubleOrNull()
        val description = binding.productDescriptionInput.text?.toString()?.trim().orEmpty()
        val category = binding.productCategoryInput.text?.toString()?.trim().orEmpty()
        val stock = binding.productStockInput.text?.toString()?.toIntOrNull()

        when {
            name.isEmpty() -> {
                binding.productNameLayout.error = getString(R.string.error_product_name_required)
                isValid = false
            }
            name.length < MIN_PRODUCT_NAME_LENGTH -> {
                binding.productNameLayout.error = getString(R.string.error_product_name_short)
                isValid = false
            }
        }

        if (price == null || price <= MIN_PRICE) {
            binding.productPriceLayout.error = getString(R.string.error_product_price_invalid)
            isValid = false
        }

        if (description.isEmpty()) {
            binding.productDescriptionLayout.error = getString(R.string.error_product_description_required)
            isValid = false
        }

        if (category.isEmpty()) {
            binding.productCategoryLayout.error = getString(R.string.error_product_category_required)
            isValid = false
        }

        if (stock == null || stock < MIN_STOCK) {
            binding.productStockLayout.error = getString(R.string.error_product_stock_invalid)
            isValid = false
        }

        return isValid
    }

    private fun clearErrors() {
        binding.productNameLayout.error = null
        binding.productPriceLayout.error = null
        binding.productDescriptionLayout.error = null
        binding.productCategoryLayout.error = null
        binding.productStockLayout.error = null
    }

    companion object {
        const val ARG_PRODUCT_ID = "productId"
        private const val DEFAULT_PRODUCT_ID = 1
        private const val MIN_PRODUCT_NAME_LENGTH = 3
        private const val MIN_PRICE = 0.0
        private const val MIN_STOCK = 0
    }
}
