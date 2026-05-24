package com.project.store.data.model

import androidx.annotation.Keep

/**
 * Product - Modelo de producto disponible en el catalogo.
 * Se almacena en la coleccion "products" de Firestore.
 * Los valores por defecto permiten la deserializacion requerida por Firestore.
 *
 * @property id Identificador unico del producto.
 * @property name Nombre comercial del producto.
 * @property description Descripcion detallada para el comprador.
 * @property price Precio unitario del producto.
 * @property stock Cantidad disponible para la venta.
 * @property sellerId UID del vendedor propietario del producto.
 * @property imageUrl URL remota o imagen codificada en Base64.
 * @property category Categoria principal del producto.
 * @property createdAt Fecha de creacion en milisegundos Unix.
 */
@Keep
data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val stock: Int = 0,
    val sellerId: String = "",
    val imageUrl: String = "",
    val category: String = "",
    val createdAt: Long = 0L
)
