package com.project.store.models

data class Product(
    val id: Int,
    val name: String,
    val description: String,
    val price: Double,
    val stock: Int,
    val categoryId: Int,
    val sellerId: Int,
    val imageName: String,
    val isAvailable: Boolean = true
)
