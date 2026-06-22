package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreDao {
    @Query("SELECT * FROM stores ORDER BY id DESC")
    fun getAllStoresFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE isApproved = 1 ORDER BY rating DESC")
    fun getApprovedStoresFlow(): Flow<List<StoreEntity>>

    @Query("SELECT * FROM stores WHERE id = :id")
    suspend fun getStoreById(id: Int): StoreEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStore(store: StoreEntity): Long

    @Update
    suspend fun updateStore(store: StoreEntity)

    @Query("UPDATE stores SET totalRevenue = totalRevenue + :amount, totalOrders = totalOrders + 1 WHERE id = :storeId")
    suspend fun recordStoreSale(storeId: Int, amount: Double)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products ORDER BY id DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId ORDER BY id DESC")
    fun getProductsByStoreFlow(storeId: Int): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE storeId = :storeId")
    suspend fun getProductsByStore(storeId: Int): List<ProductEntity>

    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    suspend fun searchProducts(query: String): List<ProductEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<ProductEntity>)

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Delete
    suspend fun deleteProduct(product: ProductEntity)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY timestamp DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE storeId = :storeId ORDER BY timestamp DESC")
    fun getOrdersByStoreFlow(storeId: Int): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderByIdFlow(orderId: Int): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderById(orderId: Int): OrderEntity?

    @Query("SELECT * FROM orders WHERE riderId = :riderId ORDER BY timestamp DESC")
    fun getOrdersByRiderFlow(riderId: Int): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Int, status: String)

    @Query("UPDATE orders SET riderLat = :lat, riderLng = :lng WHERE id = :orderId")
    suspend fun updateOrderRiderLocation(orderId: Int, lat: Double, lng: Double)
}

@Dao
interface RiderDao {
    @Query("SELECT * FROM riders")
    fun getAllRidersFlow(): Flow<List<RiderEntity>>

    @Query("SELECT * FROM riders WHERE id = :id")
    suspend fun getRiderById(id: Int): RiderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRider(rider: RiderEntity): Long

    @Update
    suspend fun updateRider(rider: RiderEntity)

    @Query("UPDATE riders SET earnings = earnings + :amount WHERE id = :riderId")
    suspend fun updateRiderEarnings(riderId: Int, amount: Double)
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chats WHERE orderId = :orderId ORDER BY timestamp ASC")
    fun getChatByOrderFlow(orderId: Int): Flow<List<ChatEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatEntity)
}
