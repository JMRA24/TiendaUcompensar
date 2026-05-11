package com.project.store.utils

import com.project.store.models.OrderItem
import com.project.store.models.Product

object CartManager {

    private val cartItems = mutableListOf(
        OrderItem(productId = 1, quantity = 1, unitPrice = 159900.0),
        OrderItem(productId = 6, quantity = 2, unitPrice = 89900.0)
    )

    fun getItems(): List<OrderItem> = cartItems.toList()

    fun addProduct(product: Product) {
        val currentIndex = cartItems.indexOfFirst { it.productId == product.id }
        if (currentIndex >= 0) {
            val current = cartItems[currentIndex]
            cartItems[currentIndex] = current.copy(quantity = current.quantity + 1)
        } else {
            cartItems.add(
                OrderItem(
                    productId = product.id,
                    quantity = 1,
                    unitPrice = product.price
                )
            )
        }
    }

    fun getSubtotal(): Double = cartItems.sumOf { it.subtotal }

    fun getShipping(): Double = if (cartItems.isEmpty()) 0.0 else SHIPPING_COST

    fun getTotal(): Double = getSubtotal() + getShipping()

    private const val SHIPPING_COST = 9900.0
}
