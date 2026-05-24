package com.project.store.data.model

import androidx.annotation.Keep

/**
 * User - Modelo de usuario de la aplicacion.
 * Se almacena en la coleccion "users" de Firestore.
 * Los valores por defecto permiten la deserializacion requerida por Firestore.
 *
 * @property id Identificador unico del usuario, normalmente el UID de Firebase Auth.
 * @property email Correo electronico asociado a la cuenta.
 * @property name Nombre visible del usuario.
 * @property role Rol funcional dentro de la aplicacion: buyer, seller o admin.
 * @property photoUrl URL o imagen codificada en Base64 para el avatar del usuario.
 * @property phone Numero telefonico de contacto.
 * @property createdAt Fecha de creacion en milisegundos Unix.
 */
@Keep
data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val role: String = "buyer",
    val photoUrl: String = "",
    val phone: String = "",
    val createdAt: Long = 0L
)
