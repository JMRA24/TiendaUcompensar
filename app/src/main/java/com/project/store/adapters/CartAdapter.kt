package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.R
import com.project.store.databinding.ItemCartBinding
import com.project.store.models.OrderItem
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class CartAdapter(
    private var items: List<OrderItem>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    fun submitList(newItems: List<OrderItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class CartViewHolder(
        private val binding: ItemCartBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        fun bind(item: OrderItem) {
            val product = MockRepository.findProductById(item.productId)
            binding.cartProductImage.setImageResource(R.drawable.ic_placeholder_product)
            binding.cartProductName.text = product?.name.orEmpty()
            binding.cartProductQuantity.text = binding.root.context.getString(
                R.string.format_quantity,
                item.quantity
            )
            binding.cartProductUnitPrice.text = currencyFormatter.format(item.unitPrice)
            binding.cartProductSubtotal.text = currencyFormatter.format(item.subtotal)
        }
    }
}
