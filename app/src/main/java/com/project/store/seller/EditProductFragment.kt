package com.project.store.seller

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.project.store.R
import com.project.store.data.model.Product
import com.project.store.data.repository.FirebaseRepository
import com.project.store.databinding.FragmentEditProductBinding
import com.project.store.utils.loadProductImage
import kotlinx.coroutines.launch

class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!
    private val repository = FirebaseRepository.getInstance()
    private var product: Product? = null
    private var requestedProductId: Int = DEFAULT_PRODUCT_ID
    private var selectedImageUri: Uri? = null
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            showSelectedImagePreview(it)
        }
    }

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
        requestedProductId = arguments?.getInt(ARG_PRODUCT_ID) ?: DEFAULT_PRODUCT_ID
        loadProduct()
        binding.chooseImageButton.setOnClickListener {
            pickImage.launch("image/*")
        }
        binding.saveProductButton.setOnClickListener {
            if (validateForm()) {
                saveProduct()
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun bindProduct() {
        val currentProduct = product ?: return

        binding.productNameInput.setText(currentProduct.name)
        binding.productPriceInput.setText(currentProduct.price.toInt().toString())
        binding.productDescriptionInput.setText(currentProduct.description)
        binding.productCategoryInput.setText(currentProduct.category)
        binding.productStockInput.setText(currentProduct.stock.toString())
        if (currentProduct.imageUrl.isNotBlank()) {
            binding.productImage.loadProductImage(currentProduct.imageUrl)
        }
    }

    private fun loadProduct() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getProductsBySeller(sellerId)
                .onSuccess { products ->
                    product = products.firstOrNull { it.id.hashCode() == requestedProductId }
                    bindProduct()
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

    private fun saveProduct() {
        val sellerId = repository.getCurrentFirebaseUser()?.uid.orEmpty()
        val currentProduct = product
        val imageBase64 = selectedImageUri?.let {
            repository.encodeImageToBase64(it, requireContext())
        }
        val productToSave = Product(
            id = currentProduct?.id.orEmpty(),
            name = binding.productNameInput.text?.toString()?.trim().orEmpty(),
            description = binding.productDescriptionInput.text?.toString()?.trim().orEmpty(),
            price = binding.productPriceInput.text?.toString()?.toDoubleOrNull() ?: 0.0,
            stock = binding.productStockInput.text?.toString()?.toIntOrNull() ?: 0,
            sellerId = sellerId,
            imageUrl = imageBase64 ?: currentProduct?.imageUrl.orEmpty(),
            category = binding.productCategoryInput.text?.toString()?.trim().orEmpty(),
            createdAt = currentProduct?.createdAt ?: System.currentTimeMillis()
        )

        setLoading(true)
        viewLifecycleOwner.lifecycleScope.launch {
            val result = if (currentProduct == null) {
                repository.saveProduct(productToSave)
            } else {
                repository.updateProduct(productToSave)
            }
            setLoading(false)
            result
                .onSuccess {
                    Toast.makeText(requireContext(), R.string.product_updated_success, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                .onFailure { error ->
                    if (error.message?.contains("UNAVAILABLE") == true ||
                        error.message?.contains("network") == true
                    ) {
                        Toast.makeText(requireContext(), R.string.error_network, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.saveProductButton.isEnabled = !isLoading
        binding.productNameInput.isEnabled = !isLoading
        binding.productPriceInput.isEnabled = !isLoading
        binding.productDescriptionInput.isEnabled = !isLoading
        binding.productCategoryInput.isEnabled = !isLoading
        binding.productStockInput.isEnabled = !isLoading
        binding.chooseImageButton.isEnabled = !isLoading
    }

    private fun showSelectedImagePreview(uri: Uri) {
        binding.productImage.setImageURI(uri)
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
