package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stores")
data class StoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String, // "Restaurant", "Grocery", "Bakery", "Pharmacy", "Retail"
    val rating: Float,
    val image: String,
    val address: String,
    val isApproved: Boolean = false,
    val kycStatus: String = "APPROVED", // "SUBMITTED", "APPROVED", "REJECTED"
    val pan: String = "",
    val gst: String = "",
    val license: String = "",
    val ownerName: String = "",
    val totalRevenue: Double = 0.0,
    val totalOrders: Int = 0
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeId: Int,
    val name: String,
    val description: String,
    val price: Double,
    val discountPercent: Int = 0,
    val category: String,
    val image: String,
    val stockLevel: Int = 50,
    val isOutOfStock: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val storeId: Int,
    val storeName: String,
    val itemsJson: String, // JSON array of CartItem
    val subtotal: Double,
    val deliveryFee: Double,
    val tax: Double,
    val platformFee: Double,
    val tip: Double,
    val totalAmount: Double,
    val status: String, // "PENDING", "PREPARING", "READY", "ON_THE_WAY", "DELIVERED", "REJECTED"
    val timestamp: Long = System.currentTimeMillis(),
    val deliveryAddress: String,
    val riderId: Int? = null,
    val riderName: String? = null,
    val riderLat: Double = 0.0,
    val riderLng: Double = 0.0,
    val orderRating: Float? = null,
    val orderReview: String? = null
)

@Entity(tableName = "riders")
data class RiderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val vehicleType: String, // "Bicycle", "Bike", "EV", "Car"
    val status: String = "ACTIVE", // "OFFLINE", "ACTIVE", "DELIVERING"
    val rating: Float = 4.8f,
    val earnings: Double = 0.0,
    val isApproved: Boolean = true,
    val pan: String = "",
    val license: String = "",
    val latitude: Double = 12.9716, // Bangalore default lat
    val longitude: Double = 77.5946 // Bangalore default lng
)

@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderId: Int,
    val sender: String, // "CUSTOMER", "RIDER"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class CartItem(
    val productId: Int,
    val productName: String,
    val price: Double,
    val quantity: Int,
    val image: String
)
