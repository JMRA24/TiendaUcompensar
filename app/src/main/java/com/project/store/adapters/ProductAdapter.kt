package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.R
import com.project.store.databinding.ItemProductBinding
import com.project.store.models.Product
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class ProductAdapter(
    private var products: List<Product> = MockRepository.products,
    private val onProductClick: (Product) -> Unit = {}
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding, onProductClick)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun submitList(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }

    class ProductViewHolder(
        private val binding: ItemProductBinding,
        private val onProductClick: (Product) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        fun bind(product: Product) {
            binding.productImage.setImageResource(R.drawable.ic_placeholder_product)
            binding.productName.text = product.name
            binding.productDescription.text = product.description
            binding.productPrice.text = currencyFormatter.format(product.price)
            binding.productStock.text = binding.root.context.getString(
                R.string.product_in_stock,
                product.stock
            )
            binding.root.setOnClickListener { onProductClick(product) }
        }
    }
}
