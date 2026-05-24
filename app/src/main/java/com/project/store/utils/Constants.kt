package com.project.store.utils

/**
 * Constants - Constantes globales de la aplicacion.
 * Centraliza valores magicos para facilitar mantenimiento.
 */
object Constants {
    // Colecciones Firestore
    const val COLLECTION_USERS = "users"
    const val COLLECTION_PRODUCTS = "products"
    const val COLLECTION_ORDERS = "orders"
    const val COLLECTION_PAYMENTS = "payments"

    // Roles de usuario
    const val ROLE_BUYER = "buyer"
    const val ROLE_SELLER = "seller"
    const val ROLE_ADMIN = "admin"

    // Estados de orden
    const val ORDER_PENDING = "pending"
    const val ORDER_PAID = "paid"
    const val ORDER_SHIPPED = "shipped"
    const val ORDER_DELIVERED = "delivered"

    // Estados de pago
    const val PAYMENT_APPROVED = "approved"
    const val PAYMENT_REJECTED = "rejected"
    const val PAYMENT_PENDING = "pending"

    // Configuracion
    const val MIN_PASSWORD_LENGTH = 6
    const val IMAGE_MAX_SIZE = 400
    const val IMAGE_QUALITY = 70
    const val PAYMENT_SUCCESS_RATE = 9
    const val LOCATION_TIMEOUT_MS = 10_000L
}
