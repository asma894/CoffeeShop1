package com.example.coffeeshop.model

data class OrderModel(
    val orderId: String = "",
    val userId: String = "",
    val userEmail: String = "",
    val items: MutableList<ItemsModel> = mutableListOf(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",
    val orderDate: String = "",
    val status: String = "Pending"
)