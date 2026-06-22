package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application.applicationContext)

    // Current Role: "CUSTOMER", "VENDOR", "RIDER", "ADMIN"
    private val _currentRole = MutableStateFlow("CUSTOMER")
    val currentRole: StateFlow<String> = _currentRole.asStateFlow()

    // --- Customer State ---
    val allStores: StateFlow<List<StoreEntity>> = repository.storeDao.getAllStoresFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    val approvedStores: StateFlow<List<StoreEntity>> = repository.storeDao.getApprovedStoresFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.orderDao.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    private val _selectedStoreId = MutableStateFlow<Int?>(null)
    val selectedStoreId: StateFlow<Int?> = _selectedStoreId.asStateFlow()

    val selectedStoreProducts: StateFlow<List<ProductEntity>> = _selectedStoreId
        .flatMapLatest { id ->
            if (id != null) repository.productDao.getProductsByStoreFlow(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
    val cartStoreId: StateFlow<Int?> = repository.cartStoreId

    // AI Search States
    private val _aiSearchQuery = MutableStateFlow("")
    val aiSearchQuery: StateFlow<String> = _aiSearchQuery.asStateFlow()

    private val _aiSearchResult = MutableStateFlow<AISearchResult?>(null)
    val aiSearchResult: StateFlow<AISearchResult?> = _aiSearchResult.asStateFlow()

    private val _isAISearching = MutableStateFlow(false)
    val isAISearching: StateFlow<Boolean> = _isAISearching.asStateFlow()

    // Live Tracking Order
    private val _activeTrackOrderId = MutableStateFlow<Int?>(null)
    val activeTrackOrderId: StateFlow<Int?> = _activeTrackOrderId.asStateFlow()

    val trackingOrder: StateFlow<OrderEntity?> = _activeTrackOrderId
        .flatMapLatest { id ->
            if (id != null) repository.orderDao.getOrderByIdFlow(id)
            else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), null)

    val activeComments: StateFlow<List<ChatEntity>> = _activeTrackOrderId
        .flatMapLatest { id ->
            if (id != null) repository.chatDao.getChatByOrderFlow(id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    // --- Vendor State ---
    // For convenience, we assume Vendor is managing the "Spice Garden Restaurant" (id=1) by default
    private val _vendorStoreId = MutableStateFlow(1)
    val vendorStoreId: StateFlow<Int> = _vendorStoreId.asStateFlow()

    val vendorStore: StateFlow<StoreEntity?> = _vendorStoreId
        .map { id -> repository.storeDao.getStoreById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), null)

    val vendorOrders: StateFlow<List<OrderEntity>> = _vendorStoreId
        .flatMapLatest { id -> repository.orderDao.getOrdersByStoreFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    val vendorProducts: StateFlow<List<ProductEntity>> = _vendorStoreId
        .flatMapLatest { id -> repository.productDao.getProductsByStoreFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    // --- Rider State ---
    // Assume Rider is Rahul Kumar (id=1) by default
    private val _activeRiderId = MutableStateFlow(1)
    val activeRiderId: StateFlow<Int> = _activeRiderId.asStateFlow()

    val activeRider: StateFlow<RiderEntity?> = _activeRiderId
        .map { id -> repository.riderDao.getRiderById(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), null)

    val riderOrders: StateFlow<List<OrderEntity>> = _activeRiderId
        .flatMapLatest { id -> repository.orderDao.getOrdersByRiderFlow(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    val allRiders: StateFlow<List<RiderEntity>> = repository.riderDao.getAllRidersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5), emptyList())

    // Simulated tracking animation jobs
    private val trackingJobs = mutableMapOf<Int, Job>()

    fun changeRole(role: String) {
        _currentRole.value = role
    }

    fun selectStore(storeId: Int?) {
        _selectedStoreId.value = storeId
        _selectedCategory.value = "All"
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    // --- Cart operations ---
    fun addToCart(product: ProductEntity) {
        repository.addToCart(product)
    }

    fun removeFromCart(productId: Int) {
        repository.decrementInCart(productId)
    }

    fun clearCart() {
        repository.clearCart()
    }

    // --- Checkout ---
    fun checkout(deliveryAddress: String, tipAmount: Double) {
        val items = cartItems.value
        val storeId = cartStoreId.value
        if (items.isEmpty() || storeId == null) return

        viewModelScope.launch(Dispatchers.IO) {
            val store = repository.storeDao.getStoreById(storeId) ?: return@launch
            val subtotal = items.sumOf { it.price * it.quantity }
            val deliveryFee = 35.0 // Flat rate delivery fee
            val tax = subtotal * 0.05 // 5% GST
            val platformFee = 5.0
            val total = subtotal + deliveryFee + tax + platformFee + tipAmount

            val newOrder = OrderEntity(
                storeId = storeId,
                storeName = store.name,
                itemsJson = repository.serializeCart(items),
                subtotal = subtotal,
                deliveryFee = deliveryFee,
                tax = tax,
                platformFee = platformFee,
                tip = tipAmount,
                totalAmount = total,
                status = "PENDING",
                deliveryAddress = deliveryAddress,
                riderId = null,
                riderName = null,
                riderLat = 12.9716, // Bangalore default center
                riderLng = 77.5946
            )

            val orderId = repository.orderDao.insertOrder(newOrder).toInt()
            
            // Set as active tracking order
            _activeTrackOrderId.value = orderId
            
            // Clear current cart
            repository.clearCart()
        }
    }

    // --- Order Status updates & simulated coordinate movement ---
    fun updateOrderStatus(orderId: Int, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val order = repository.orderDao.getOrderById(orderId) ?: return@launch
            
            // If order is rejected, clear rider assignment if any
            if (newStatus == "REJECTED") {
                repository.orderDao.updateOrder(order.copy(status = "REJECTED"))
                return@launch
            }

            // Perform specific role transition updates
            if (newStatus == "ON_THE_WAY") {
                // If moving to ON_THE_WAY, start rider coordinate tracking animation!
                repository.orderDao.updateOrder(order.copy(status = "ON_THE_WAY"))
                startSimulatedRiderDelivery(orderId)
            } else if (newStatus == "DELIVERED") {
                // Settle financials: add revenue to vendor total, add earnings to rider total
                repository.orderDao.updateOrder(order.copy(status = "DELIVERED"))
                repository.storeDao.recordStoreSale(order.storeId, order.subtotal)
                
                order.riderId?.let { rId ->
                    val earnings = order.deliveryFee + order.tip
                    repository.riderDao.updateRiderEarnings(rId, earnings)
                }
                
                // Cancel coordinate tracking job
                trackingJobs[orderId]?.cancel()
                trackingJobs.remove(orderId)
            } else {
                repository.orderDao.updateOrderStatus(orderId, newStatus)
            }
        }
    }

    // Rider accepts PENDING/READY orders
    fun riderAcceptOrder(orderId: Int, riderId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val rider = repository.riderDao.getRiderById(riderId) ?: return@launch
            val order = repository.orderDao.getOrderById(orderId) ?: return@launch

            val updatedOrder = order.copy(
                status = order.status, // preserve the current preparation status
                riderId = riderId,
                riderName = rider.name,
                riderLat = rider.latitude,
                riderLng = rider.longitude
            )
            repository.orderDao.updateOrder(updatedOrder)
            
            // Send system message in chat
            repository.chatDao.insertChat(ChatEntity(
                orderId = orderId,
                sender = "RIDER",
                message = "Hi! I have accepted your order and am heading to the store."
            ))
        }
    }

    private fun startSimulatedRiderDelivery(orderId: Int) {
        // Cancel existing job
        trackingJobs[orderId]?.cancel()

        val job = viewModelScope.launch(Dispatchers.IO) {
            val startLat = 12.9716 // Store coordinate (bangalore center)
            val startLng = 77.5946
            val destLat = 12.9925 // Customer default coordinate
            val destLng = 77.6240

            val steps = 10
            for (i in 1..steps) {
                if (trackingOrder.value == null || trackingOrder.value?.status != "ON_THE_WAY") break

                val ratio = i.toDouble() / steps.toDouble()
                val currentLat = startLat + (destLat - startLat) * ratio
                val currentLng = startLng + (destLng - startLng) * ratio

                repository.orderDao.updateOrderRiderLocation(orderId, currentLat, currentLng)
                
                // Keep customer updated in chat occasionally
                if (i == 4) {
                    repository.chatDao.insertChat(ChatEntity(
                        orderId = orderId,
                        sender = "RIDER",
                        message = "I am halfway there! Fresh and hot, see you in 5 mins."
                    ))
                } else if (i == 8) {
                    repository.chatDao.insertChat(ChatEntity(
                        orderId = orderId,
                        sender = "RIDER",
                        message = "Nearly there, turning onto your street now!"
                    ))
                }

                delay(4000) // Delay 4 seconds to animate nice and visibly in UI
            }
        }
        trackingJobs[orderId] = job
    }

    // --- Chats ---
    fun sendChatMessage(orderId: Int, sender: String, text: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.chatDao.insertChat(ChatEntity(
                orderId = orderId,
                sender = sender,
                message = text
            ))
        }
    }

    // --- AI Natural Language Search with Gemini ---
    fun searchWithAI(query: String) {
        if (query.trim().isEmpty()) {
            _aiSearchResult.value = null
            _aiSearchQuery.value = ""
            return
        }
        _aiSearchQuery.value = query
        _isAISearching.value = true

        viewModelScope.launch {
            try {
                val result = repository.performAISearch(query)
                _aiSearchResult.value = result
            } catch (e: Exception) {
                _aiSearchResult.value = AISearchResult(
                    explanation = "An unexpected error occurred during AI search.",
                    products = emptyList(),
                    isRealAI = false
                )
            } finally {
                _isAISearching.value = false
            }
        }
    }

    // --- Store and Product actions for Vendor/Admin ---
    fun addStore(store: StoreEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.storeDao.insertStore(store)
        }
    }

    fun updateStoreApproval(storeId: Int, isApproved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val store = repository.storeDao.getStoreById(storeId) ?: return@launch
            repository.storeDao.updateStore(store.copy(isApproved = isApproved, kycStatus = if (isApproved) "APPROVED" else "REJECTED"))
        }
    }

    fun addProduct(product: ProductEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.productDao.insertProduct(product)
        }
    }

    fun updateProductStock(productId: Int, newStock: Int, isOutOfStock: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            // Find product and update. Since we only have Flow, we can fetching using list
            val products = repository.productDao.getAllProductsFlow().first()
            val prod = products.find { it.id == productId } ?: return@launch
            repository.productDao.updateProduct(prod.copy(stockLevel = newStock, isOutOfStock = isOutOfStock))
        }
    }

    // Submit review and rating for an completed order
    fun rateOrder(orderId: Int, rating: Float, review: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val order = repository.orderDao.getOrderById(orderId) ?: return@launch
            repository.orderDao.updateOrder(order.copy(orderRating = rating, orderReview = review))
        }
    }

    fun setTrackingOrderId(orderId: Int?) {
        _activeTrackOrderId.value = orderId
    }

    override fun onCleared() {
        super.onCleared()
        trackingJobs.values.forEach { it.cancel() }
        trackingJobs.clear()
    }
}
