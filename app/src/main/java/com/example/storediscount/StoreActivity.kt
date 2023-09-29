package com.example.storediscount

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.objects.Product
import com.example.objects.ProductAdapter
import com.example.objects.Store
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatDelegate

class StoreActivity : Activity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setContentView(R.layout.activity_store) // Make sure to set the correct layout

        val backToMainButton = findViewById<Button>(R.id.backToMain)
        backToMainButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // This closes the current StoreActivity
        }
        val storeName = intent.getStringExtra("ABC")

        val spinner: Spinner = findViewById(R.id.spinner)
        // Create an ArrayAdapter using the string array and a default spinner layout
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        )
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Apply the adapter to the spinner
        spinner.adapter = adapter

        //val products = GetStoresAPI(storeName.toString())

        // Deserialize products list from the string passed in the intent
        val gson = Gson()
        val productsJson = intent.getStringExtra("PRODUCT_LIST")
        val type = object : TypeToken<MutableList<Product>>() {}.type
        val productList: MutableList<Product> = gson.fromJson(productsJson, type)
        Log.i("ABC", productList.toString())

        // STORELIST
        val storeJson = intent.getStringExtra("STORE_LIST")
        val storeType = object : TypeToken<MutableList<Store>>() {}.type
        val storeList: MutableList<Product> = gson.fromJson(storeJson, storeType)
        Log.i("ABC", storeList.toString())

        productList.sortBy { it.newPrice }


        val recyclerView: RecyclerView = findViewById(R.id.productRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ProductAdapter(productList)



        // Set item selected listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (parent?.getItemAtPosition(position).toString()) {
                    "Sort by name" -> productList.sortBy { it.name }
                    "Sort by price" -> productList.sortBy { it.newPrice }
                    "Sort by discount %" -> productList.sortByDescending  { it.discountPercent }
                }
                // Notify the adapter that the dataset has changed
                recyclerView.adapter?.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing here
            }
        }

    }

    fun extractProductDetails(product: JSONObject, storeName: String): Product {
        // Extracting offer details
        val offer = product.getJSONObject("offer")
        val endTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(offer.getString("endTime"))
        val startTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").parse(offer.getString("startTime"))
        val newPrice = offer.getInt("newPrice")
        val originalPrice = offer.getInt("originalPrice")
        val discountKrones = originalPrice - newPrice
        val discountPercent = offer.getInt("percentDiscount")
        val stockLeft = offer.getInt("stock")

        // Extracting product details
        val productDetails = product.getJSONObject("product")
        val name = productDetails.getString("description")
        val pictureLink = productDetails.getString("image")
        val categories = productDetails.getJSONObject("categories")
        val categoryEnglish = if (categories.has("en")) categories.getString("en") else ""
        val categoryDanish = if (categories.has("da")) categories.getString("da") else ""


        // Creating and returning the Product object
        return Product(
            name, pictureLink, categoryEnglish, categoryDanish, discountKrones,
            discountPercent, originalPrice, stockLeft, startTime, endTime, newPrice, storeName
        )
    }

}
