package com.project.store.models

data class Order(
    val id: Int,
    val buyerId: Int,
    val products: List<OrderItem>,
    val status: OrderStatus,
    val orderDate: String,
    val shippingAddress: String
) {
    val total: Double
        get() = products.sumOf { it.subtotal }
}

data class OrderItem(
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double
) {
    val subtotal: Double
        get() = quantity * unitPrice
}

enum class OrderStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
