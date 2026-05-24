package com.project.store.data.model

import androidx.annotation.Keep

/**
 * Payment - Modelo de pago simulado.
 * Se almacena en la coleccion "payments" de Firestore.
 * Los valores por defecto permiten la deserializacion requerida por Firestore.
 *
 * @property id Identificador unico del pago.
 * @property orderId Identificador de la orden pagada.
 * @property buyerId UID del comprador que realiza el pago.
 * @property amount Valor total procesado.
 * @property method Metodo de pago seleccionado: card, pse o nequi.
 * @property transactionId Codigo corto de transaccion generado por la simulacion.
 * @property status Estado del pago: pending, approved o rejected.
 * @property createdAt Fecha de creacion en milisegundos Unix.
 */
@Keep
data class Payment(
    val id: String = "",
    val orderId: String = "",
    val buyerId: String = "",
    val amount: Double = 0.0,
    val method: String = "",
    val transactionId: String = "",
    val status: String = "pending",
    val createdAt: Long = 0L
)
