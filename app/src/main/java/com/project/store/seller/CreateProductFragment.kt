package com.project.store.seller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.databinding.FragmentCreateProductBinding

class CreateProductFragment : Fragment() {

    private var _binding: FragmentCreateProductBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateProductBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.chooseImageButton.setOnClickListener {
            Toast.makeText(requireContext(), R.string.product_image_mock_selected, Toast.LENGTH_SHORT).show()
        }
        binding.saveProductButton.setOnClickListener {
            if (validateForm()) {
                Toast.makeText(requireContext(), R.string.product_created_success, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun validateForm(): Boolean {
        clearErrors()
        var isValid = true

        val name = binding.productNameInput.text?.toString()?.trim().orEmpty()
        val price = binding.productPriceInput.text?.toString()?.toDoubleOrNull()
        val description = binding.productDescriptionInput.text?.toString()?.trim().orEmpty()
        val category = binding.productCategoryInput.text?.toString()?.trim().orEmpty()
        val stock = binding.productStockInput.text?.toString()?.toIntOrNull()
        val discountText = binding.productDiscountInput.text?.toString()?.trim().orEmpty()
        val discount = discountText.toIntOrNull()

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

        if (discountText.isNotEmpty() && (discount == null || discount !in MIN_DISCOUNT..MAX_DISCOUNT)) {
            binding.productDiscountLayout.error = getString(R.string.error_product_discount_invalid)
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
        binding.productDiscountLayout.error = null
    }

    companion object {
        private const val MIN_PRODUCT_NAME_LENGTH = 3
        private const val MIN_PRICE = 0.0
        private const val MIN_STOCK = 0
        private const val MIN_DISCOUNT = 0
        private const val MAX_DISCOUNT = 99
    }
}
