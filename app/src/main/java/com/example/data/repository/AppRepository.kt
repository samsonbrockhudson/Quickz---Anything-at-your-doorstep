package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.dao.*
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AppRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    val storeDao = database.storeDao()
    val productDao = database.productDao()
    val orderDao = database.orderDao()
    val riderDao = database.riderDao()
    val chatDao = database.chatDao()

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val cartItemsAdapter = moshi.adapter<List<CartItem>>(
        Types.newParameterizedType(List::class.java, CartItem::class.java)
    )

    // For Cart State
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _cartStoreId = MutableStateFlow<Int?>(null)
    val cartStoreId: StateFlow<Int?> = _cartStoreId.asStateFlow()

    init {
        // Run pre-population on a background thread
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            prepopulateIfEmpty()
        }
    }

    // --- Prepopulation logic ---
    private suspend fun prepopulateIfEmpty() {
        val stores = storeDao.getAllStoresFlow().first()
        if (stores.isEmpty()) {
            Log.d("AppRepository", "Database empty. Populating default data...")
            
            // Insert default Riders
            val rider1 = RiderEntity(name = "Rahul Kumar", vehicleType = "Bike", status = "ACTIVE", rating = 4.9f, pan = "ABCDE1234F", license = "DL-202610293")
            val rider2 = RiderEntity(name = "Amit Sharma", vehicleType = "EV", status = "ACTIVE", rating = 4.8f, pan = "FGHIJ5678K", license = "DL-202622930")
            val rider3 = RiderEntity(name = "Deepak Singh", vehicleType = "Bicycle", status = "ACTIVE", rating = 4.7f, pan = "LMNOP9012Q", license = "DL-202646271")
            
            riderDao.insertRider(rider1)
            riderDao.insertRider(rider2)
            riderDao.insertRider(rider3)

            // Insert default Stores
            val store1Id = storeDao.insertStore(StoreEntity(
                name = "Spice Garden Restaurant",
                type = "Restaurant",
                rating = 4.6f,
                image = "restaurant_spice_garden",
                address = "Koramangala, Bangalore",
                isApproved = true,
                ownerName = "Chef Sameer Verma",
                pan = "PANSPICEGM1",
                gst = "29GSTSPICE1234",
                license = "FSSAI-12002300"
            )).toInt()

            val store2Id = storeDao.insertStore(StoreEntity(
                name = "FreshInsta Grocery",
                type = "Grocery",
                rating = 4.8f,
                image = "grocery_freshinsta",
                address = "Indiranagar, Bangalore",
                isApproved = true,
                ownerName = "Vijay Singhal",
                pan = "PANFRESHGD2",
                gst = "29GSTFRESH5678",
                license = "LICENSE-49033"
            )).toInt()

            val store3Id = storeDao.insertStore(StoreEntity(
                name = "The Golden Crust Bakery",
                type = "Bakery",
                rating = 4.5f,
                image = "bakery_golden_crust",
                address = "HSR Layout, Bangalore",
                isApproved = true,
                ownerName = "Anisha Roy",
                pan = "PANGOLDCR3",
                gst = "29GSTGOLD9012",
                license = "LICENSE-89022"
            )).toInt()

            val store4Id = storeDao.insertStore(StoreEntity(
                name = "Apollo Pharmacy Lite",
                type = "Pharmacy",
                rating = 4.7f,
                image = "pharmacy_apollo",
                address = "Whitefield, Bangalore",
                isApproved = true,
                ownerName = "Dr. Shashi Kant",
                pan = "PANAPOLLP4",
                gst = "29GSTAPOL3456",
                license = "PHARM-99088"
            )).toInt()

            val store5Id = storeDao.insertStore(StoreEntity(
                name = "Halal & Fresh Meat Shop",
                type = "Meat & Fish",
                rating = 4.4f,
                image = "meat_halal_fresh",
                address = "BTM Layout, Bangalore",
                isApproved = true,
                ownerName = "Imran Khan",
                pan = "PANHALALM5",
                gst = "29GSTHALA7890",
                license = "LICENSE-12111"
            )).toInt()

            val store6Id = storeDao.insertStore(StoreEntity(
                name = "Daily Dairy Essentials",
                type = "Dairy",
                rating = 4.5f,
                image = "dairy_daily",
                address = "BTM Layout, Bangalore",
                isApproved = true,
                ownerName = "Mukul Yadav",
                pan = "PANDAIRYE6",
                gst = "29GSTDAIR4567",
                license = "LICENSE-55272"
            )).toInt()

            // Insert default products for each store
            // Restaurant products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store1Id, name = "Special Chicken Biryani", description = "Fragrant long-grain basmati rice cooked with succulent chicken, spices and saffron. Served with raita.", price = 299.0, discountPercent = 15, category = "Restaurants", image = "food_biryani"),
                ProductEntity(storeId = store1Id, name = "Butter Chicken Masala", description = "Rich, creamy, buttery tomato gravy with tandoori grilled chicken chunks.", price = 249.0, discountPercent = 10, category = "Restaurants", image = "food_butter_chicken"),
                ProductEntity(storeId = store1Id, name = "Garlic Naan", description = "Tandoor baked white-flour flatbread topped with garlic and butter.", price = 45.0, discountPercent = 0, category = "Restaurants", image = "food_garlic_naan"),
                ProductEntity(storeId = store1Id, name = "Hot & Sour Veg Soup", description = "Spicy and tangy soup with minced fresh vegetables.", price = 120.0, discountPercent = 0, category = "Restaurants", image = "food_soup")
            ))

            // Grocery products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store2Id, name = "Fresh Organic Potatoes", description = "Net weight 1 kg. Directly sourced from farmers.", price = 35.0, discountPercent = 5, category = "Fruits & Vegetables", image = "grocery_potatoes"),
                ProductEntity(storeId = store2Id, name = "Premium Vine Tomatoes", description = "Net weight 500g. Fresh, ripe and juicy.", price = 49.0, discountPercent = 10, category = "Fruits & Vegetables", image = "grocery_tomatoes"),
                ProductEntity(storeId = store2Id, name = "Aashirvaad Shudh Chakki Atta", description = "Whole wheat flour, 5 kg bag. Complete fibers and nutrition.", price = 275.0, discountPercent = 8, category = "Grocery", image = "grocery_atta"),
                ProductEntity(storeId = store2Id, name = "Fortune Soya Health Oil", description = "Refined soyabean oil, 1 Liter pouch.", price = 145.0, discountPercent = 12, category = "Grocery", image = "grocery_oil")
            ))

            // Bakery products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store3Id, name = "Choco Lava Cake", description = "Warm chocolate cake with a delicious liquid chocolate center.", price = 85.0, discountPercent = 15, category = "Bakery", image = "bakery_choco_lava"),
                ProductEntity(storeId = store3Id, name = "Artisanal Sourdough Bread", description = "Naturally fermented bread loaf with crispy crust and soft chewy center.", price = 110.0, discountPercent = 5, category = "Bakery", image = "bakery_sourdough"),
                ProductEntity(storeId = store3Id, name = "Pineapple Dream Pastry", description = "Layers of light sponge cake and whipped cream infused with sweet pineapple.", price = 60.0, discountPercent = 10, category = "Bakery", image = "bakery_pineapple_pastry")
            ))

            // Pharmacy products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store4Id, name = "Paracetamol 650mg tablets", description = "For fever, headache, bodyache relief. Box contains 15 tablets strip.", price = 30.0, discountPercent = 0, category = "Medicines", image = "pharma_paracetamol"),
                ProductEntity(storeId = store4Id, name = "Limcee Vitamin C 500mg Chewables", description = "Helps build strong immunity, delicious orange flavour. 30 tabs.", price = 120.0, discountPercent = 15, category = "Medicines", image = "pharma_vit_c"),
                ProductEntity(storeId = store4Id, name = "Complete First Aid Emergency Kit", description = "Compact box containing sterile dressings, bandages, adhesive tapes, wipes.", price = 350.0, discountPercent = 20, category = "Medicines", image = "pharma_first_aid")
            ))

            // Meat & Fish products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store5Id, name = "Premium Boneless Chicken Breast", description = "Fresh, skinless, high-protein lean cuts. 500g skin packed pack.", price = 220.0, discountPercent = 10, category = "Meat & Fish", image = "meat_chicken"),
                ProductEntity(storeId = store5Id, name = "Freshwater Bengal Rohu Fish Steaks", description = "500g packet. Neatly cleaned, scaled and cut into perfect steaks.", price = 310.0, discountPercent = 5, category = "Meat & Fish", image = "meat_rohu")
            ))

            // Dairy products
            productDao.insertProducts(listOf(
                ProductEntity(storeId = store6Id, name = "Nandini Rich Buffalo Milk Pouch", description = "500ml pouch pasteurized, thick standard fat milk.", price = 35.0, discountPercent = 0, category = "Dairy", image = "dairy_milk"),
                ProductEntity(storeId = store6Id, name = "Amul Butter - Salted", description = "100g pack. Pure creamy salted butter.", price = 55.0, discountPercent = 0, category = "Dairy", image = "dairy_butter")
            ))

            Log.d("AppRepository", "Successfully populated default databases.")
        }
    }

    // --- Cart Management ---
    fun addToCart(product: ProductEntity) {
        val currentStoreId = _cartStoreId.value
        if (currentStoreId != null && currentStoreId != product.storeId) {
            // Throw exception or clear cart when changing store. For ease we clear existing items.
            _cartItems.value = emptyList()
        }
        _cartStoreId.value = product.storeId
        val existingList = _cartItems.value.toMutableList()
        val index = existingList.indexOfFirst { it.productId == product.id }
        if (index != -1) {
            existingList[index] = existingList[index].copy(quantity = existingList[index].quantity + 1)
        } else {
            val netPrice = product.price * (1 - product.discountPercent / 100.0)
            existingList.add(CartItem(
                productId = product.id,
                productName = product.name,
                price = netPrice,
                quantity = 1,
                image = product.image
            ))
        }
        _cartItems.value = existingList
    }

    fun decrementInCart(productId: Int) {
        val existingList = _cartItems.value.toMutableList()
        val index = existingList.indexOfFirst { it.productId == productId }
        if (index != -1) {
            val currentQty = existingList[index].quantity
            if (currentQty <= 1) {
                existingList.removeAt(index)
            } else {
                existingList[index] = existingList[index].copy(quantity = currentQty - 1)
            }
        }
        _cartItems.value = existingList
        if (existingList.isEmpty()) {
            _cartStoreId.value = null
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _cartStoreId.value = null
    }

    // Convert cart list to Json String
    fun serializeCart(items: List<CartItem>): String {
        return try {
            cartItemsAdapter.toJson(items)
        } catch (e: Exception) {
            "[]"
        }
    }

    fun deserializeCart(json: String): List<CartItem> {
        return try {
            cartItemsAdapter.fromJson(json) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Gemini Natural Language AI Search ---
    suspend fun performAISearch(query: String): AISearchResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        
        // Handle Missing API Key or Default Placeholder key
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e("AppRepository", "Missing valid Gemini API key. Performing matching locally.")
            return@withContext performFallbackLocalAISearch(query)
        }

        // Fetch all products across database to build context
        val allProducts = productDao.getAllProductsFlow().first()
        val productsJsonArray = JSONArray()
        for (prod in allProducts) {
            val itemObj = JSONObject()
            itemObj.put("id", prod.id)
            itemObj.put("name", prod.name)
            itemObj.put("description", prod.description)
            itemObj.put("price", prod.price)
            itemObj.put("discountPercent", prod.discountPercent)
            itemObj.put("category", prod.category)
            productsJsonArray.put(itemObj)
        }

        val prompt = """
            You are GoLocal's Hyperlocal AI Search Assistant. 
            Below is the full catalog of products available in the user's vicinity in JSON format:
            $productsJsonArray
            
            The user is searching for: "$query".
            Your goals are:
            1. Handle potential spelling typos gracefully (e.g. "briyani" -> "Biryani", "potatos" -> "Potatoes").
            2. Match synonyms or semantic intents (e.g. "fever tablet" -> "Paracetamol 650mg tablets", "oil for cooking" -> "Fortune Soya Health Oil").
            3. Respect price constraints mentioned in the query (e.g. "cheap biryani" or "vegetables under 50" or "medicines").
            
            You must return highly relevant recommendations. 
            Respond strictly in valid JSON format matching this exact schema:
            {
               "explanation": "Brief conversational sentence explaining why these items are recommended (e.g., 'Found fresh whole wheat flour, soybean oil and potatoes that match your grocery needs.')",
               "matchingProductIds": [4, 5, 2]
            }
            
            Do NOT wrap your response in markdown code blocks like ```json ... ```. Just return raw JSON.
        """.trimIndent()

        val jsonRequest = JSONObject()
        val contentsArray = JSONArray()
        val contentObject = JSONObject()
        val partsArray = JSONArray()
        val partObject = JSONObject()
        partObject.put("text", prompt)
        partsArray.put(partObject)
        contentObject.put("parts", partsArray)
        contentsArray.put(contentObject)
        jsonRequest.put("contents", contentsArray)

        // Setup client with custom timeouts
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonRequest.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val mainJsonObj = JSONObject(responseBody)
                val candidates = mainJsonObj.getJSONArray("candidates")
                val textResponse = candidates.getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")

                // Clean the generated text in case the model added markdown blocks
                val cleanText = textResponse.replace("```json", "")
                    .replace("```", "")
                    .trim()

                val parsedResultObject = JSONObject(cleanText)
                val explanation = parsedResultObject.optString("explanation", "Matches for your search.")
                val idsArray = parsedResultObject.getJSONArray("matchingProductIds")
                val idsList = mutableListOf<Int>()
                for (i in 0 until idsArray.length()) {
                    idsList.add(idsArray.getInt(i))
                }
                
                // Fetch the product entities from database
                val matchingProducts = allProducts.filter { idsList.contains(it.id) }
                return@withContext AISearchResult(
                    explanation = explanation,
                    products = matchingProducts,
                    isRealAI = true
                )
            } else {
                Log.e("AppRepository", "Gemini API error. Code: ${response.code}, Msg: ${response.message}")
                return@withContext performFallbackLocalAISearch(query)
            }
        } catch (e: Exception) {
            Log.e("AppRepository", "Gemini API failed with exception", e)
            return@withContext performFallbackLocalAISearch(query)
        }
    }

    private suspend fun performFallbackLocalAISearch(query: String): AISearchResult {
        val allProducts = productDao.getAllProductsFlow().first()
        val keywords = query.lowercase().split(" ", ",")
        val matched = allProducts.filter { prod ->
            keywords.any { word ->
                prod.name.lowercase().contains(word) ||
                prod.description.lowercase().contains(word) ||
                prod.category.lowercase().contains(word)
            }
        }
        return AISearchResult(
            explanation = "Using smart local match: Showing products matching '${query}' in store names or categories.",
            products = matched,
            isRealAI = false
        )
    }
}

data class AISearchResult(
    val explanation: String,
    val products: List<ProductEntity>,
    val isRealAI: Boolean
)
