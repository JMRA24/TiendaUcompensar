package com.project.store.utils

import com.project.store.models.OrderItem
import com.project.store.models.Product

object CartManager {

    private val cartItems = mutableListOf<OrderItem>()

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
                    productName = product.name,
                    productImage = product.imageName,
                    quantity = 1,
                    unitPrice = product.price
                )
            )
        }
    }

    fun getSubtotal(): Double = cartItems.sumOf { it.subtotal }

    fun getShipping(): Double = if (cartItems.isEmpty()) 0.0 else SHIPPING_COST

    fun getTotal(): Double = getSubtotal() + getShipping()

    fun clear() {
        cartItems.clear()
    }

    fun removeItem(productId: Int) {
        cartItems.removeAll { it.productId == productId }
    }

    private const val SHIPPING_COST = 9900.0
}
