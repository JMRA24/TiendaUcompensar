package com.project.store.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.project.store.R
import com.project.store.databinding.ItemOrderHistoryBinding
import com.project.store.models.Order
import com.project.store.models.OrderStatus
import java.text.NumberFormat
import java.util.Locale

class OrderHistoryAdapter(
    private var orders: List<Order>
) : RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderHistoryViewHolder {
        val binding = ItemOrderHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderHistoryViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount(): Int = orders.size

    class OrderHistoryViewHolder(
        private val binding: ItemOrderHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("es", "CO"))

        fun bind(order: Order) {
            val context = binding.root.context
            binding.orderId.text = context.getString(R.string.label_order_id, order.id.toString())
            binding.orderDate.text = context.getString(R.string.label_order_date, order.orderDate)
            binding.orderItemsCount.text = context.getString(
                R.string.label_items_count,
                order.products.sumOf { it.quantity }
            )
            binding.orderTotal.text = context.getString(
                R.string.summary_total,
                currencyFormatter.format(order.total)
            )
            binding.orderStatus.text = context.getString(order.status.labelResId())
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
