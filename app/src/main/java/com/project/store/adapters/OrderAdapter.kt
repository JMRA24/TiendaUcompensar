package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.R
import com.project.store.databinding.ItemSellerOrderBinding
import com.project.store.models.Order
import com.project.store.models.OrderStatus
import com.project.store.utils.MockRepository
import java.text.NumberFormat
import java.util.Locale

class OrderAdapter(
    private var orders: List<Order> = MockRepository.orders,
    private val onOrderClick: (Order) -> Unit = {}
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemSellerOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding, onOrderClick)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    fun submitList(newOrders: List<Order>) {
        orders = newOrders
        notifyDataSetChanged()
    }

    class OrderViewHolder(
        private val binding: ItemSellerOrderBinding,
        private val onOrderClick: (Order) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        fun bind(order: Order) {
            val context = binding.root.context
            val buyerName = MockRepository.users.firstOrNull { it.id == order.buyerId }?.fullName.orEmpty()

            binding.orderId.text = context.getString(R.string.label_order_id, order.id.toString())
            binding.orderCustomer.text = context.getString(R.string.seller_order_customer, buyerName)
            binding.orderDate.text = context.getString(R.string.label_order_date, order.orderDate)
            binding.orderProducts.text = context.getString(
                R.string.seller_order_products,
                order.products.sumOf { it.quantity }
            )
            binding.orderTotal.text = context.getString(
                R.string.summary_total,
                currencyFormatter.format(order.total)
            )
            binding.orderStatus.text = context.getString(order.status.labelResId())
            binding.root.setOnClickListener { onOrderClick(order) }
        }

        private fun OrderStatus.labelResId(): Int = when (this) {
            OrderStatus.PENDING -> R.string.order_status_pending
            OrderStatus.PROCESSING -> R.string.order_status_processing
            OrderStatus.SHIPPED -> R.string.order_status_shipped
            OrderStatus.DELIVERED -> R.string.order_status_delivered
            OrderStatus.CANCELLED -> R.string.order_status_cancelled
        }
    }
}
