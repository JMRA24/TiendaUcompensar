package com.project.store.data.model

import androidx.annotation.Keep

/**
 * Order - Modelo de orden de compra.
 * Se almacena en la coleccion "orders" de Firestore.
 * Los valores por defecto permiten la deserializacion requerida por Firestore.
 *
 * @property id Identificador unico de la orden.
 * @property buyerId UID del comprador que realizo la compra.
 * @property sellerId UID del vendedor asociado al producto.
 * @property productId Identificador del producto comprado.
 * @property quantity Cantidad comprada del producto.
 * @property total Valor total de la linea de compra.
 * @property status Estado de la orden dentro del flujo de venta.
 * @property address Direccion de entrega capturada en checkout.
 * @property lat Latitud GPS de la direccion de entrega.
 * @property lng Longitud GPS de la direccion de entrega.
 * @property createdAt Fecha de creacion en milisegundos Unix.
 */
@Keep
data class Order(
    val id: String = "",
    val buyerId: String = "",
    val sellerId: String = "",
    val productId: String = "",
    val quantity: Int = 0,
    val total: Double = 0.0,
    val status: String = "",
    val address: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val createdAt: Long = 0L
)
