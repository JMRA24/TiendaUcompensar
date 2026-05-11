package com.project.store.models

data class User(
    val id: Int,
    val fullName: String,
    val email: String,
    val password: String,
    val role: UserRole,
    val phone: String,
    val isActive: Boolean = true
)

enum class UserRole {
    ADMIN,
    SELLER,
    BUYER
}
