package com.example.storediscount
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.objects.Product
import com.example.objects.Store
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions


import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat


class MainActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback  {
    private lateinit var locationManager: LocationManager
    private lateinit var tvGpsLocation: TextView
    private val locationPermissionCode = 2
    private var currentLatitude: Double? = null // Y
    private var currentLongitude: Double? = null // X
    private val dataList: MutableList<Store> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        getLocation()
        // When testing in emulator the location will go to googles address. So I hardcode it to go to CPH
        currentLatitude = 55.673018167630815
        currentLongitude = 12.543846865476018

        CoroutineScope(Dispatchers.Main).launch {
            dataList.addAll(GetStoresAPI())

            val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this@MainActivity)
        }

    }

    private fun getLocation() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
        } else {

            // Request location updates
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5f, this)

            // Get the last known location
            val location: Location? = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            location?.let {
                currentLatitude = it.latitude
                currentLongitude = it.longitude
            } ?: run {

            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onLocationChanged(location: Location) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {

        val currentLocation = currentLatitude?.let { currentLongitude?.let { it1 -> LatLng(it, it1) } }

        // Adding the current location marker and setting its tag
        val currentLocationMarker = googleMap.addMarker(
            MarkerOptions()
                .position(currentLocation)
                .title("Your current location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .snippet("This is a snippet or additional info.")
        )
        currentLocationMarker.tag = "CURRENT_LOCATION"

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 13.0f))

        for (item in dataList) {

            val storeMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(item.KoordY.toDouble(), item.KoordX.toDouble()))
                    .title(item.Name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

            )
            if (storeMarker != null) {
                storeMarker.tag = item.Name
            }
        }

        googleMap.setOnInfoWindowClickListener { marker ->
            // Check if the clicked marker's tag is "CURRENT_LOCATION"
                // Open the new Activity for current location marker only
                val intent = Intent(this@MainActivity, StoreActivity::class.java)
                intent.putExtra("ABC", marker.tag.toString())



            val storeTag = marker.tag.toString()

            // Find the store in your list which matches the marker's tag
            val matchingStore = dataList.find { it.Name == storeTag }

            if (matchingStore != null) {
                Log.i("ABC", matchingStore.ProductList.toString())

                // Pass the products list from the store to the intent

                val gson = Gson()
                val productsJson = gson.toJson(ArrayList(matchingStore.ProductList))
                val storeJson = gson.toJson(matchingStore)

                intent.putExtra("PRODUCT_LIST", productsJson)
                intent.putExtra("STORE_LIST", storeJson)


            }


            startActivity(intent)

        }
    }

    suspend fun GetStoresAPI(): List<Store> {
        val storeList: MutableList<Store> = mutableListOf()

        val response = withContext(Dispatchers.IO) {
            // Your network code here
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://api.sallinggroup.com/v1/food-waste/?geo=$currentLatitude,$currentLongitude&radius=1")
                .addHeader("Authorization", "Bearer 0793871d-0b53-44b8-94fb-b2ba6216e099")
                .addHeader("Cookie", "TiPMix=39.76039930180127; x-ms-routing-name=self")
                .build()
            client.newCall(request).execute()
        }

        val responseBody = response.body?.string()
        val jsonArray = JSONArray(responseBody)

        for (i in 0 until jsonArray.length()) {
            val productList: MutableList<Product> = mutableListOf()


            val item = jsonArray.getJSONObject(i)
            val clearance = item.getString("clearances")

            if (item.has("store")) {
                val store = item.getString("store")
                val parsedJSON = JSONObject(store)
                val name = parsedJSON.getString("name")
                val id = parsedJSON.getString("id")
                Log.i("abc", id.toString())
                val jsonArray2 = JSONArray(clearance)

                for (i in 0 until jsonArray2.length()){
                    val product = jsonArray2.getJSONObject(i)
                    val tempProductList = extractProductDetails(product, name)
                    productList.add(tempProductList)
                }


                val brand = parsedJSON.getString("brand")
                val address = parsedJSON.getString("address")

                val addressJSON = JSONObject(address)
                val city = addressJSON.getString("city")
                val street = addressJSON.getString("street")
                val zip = addressJSON.getString("zip")

                val coordinates = parsedJSON.getJSONArray("coordinates")
                val koordX = coordinates.getDouble(0).toFloat()
                val koordY = coordinates.getDouble(1).toFloat()

                val distanceAway = parsedJSON.getDouble("distance_km").toFloat()
                val hours = parsedJSON.getJSONArray("hours")
                val todayHours = hours.getJSONObject(0)
                val tomorrowHours = hours.getJSONObject(1)

                val isOpen = todayHours.getString("closed") != "true"
                val startTimeToday = todayHours.getString("open")
                val endTimeToday = todayHours.getString("close")

                val customerFlowToday = todayHours.optString("customerFlow", "NO_CUSTOMER_FLOW")
                val customerFlowTomorrow = tomorrowHours.optString("customerFlow", "NO_CUSTOMER_FLOW")


                val storeObject = Store(
                    Name = name,
                    StreetName = street,
                    Zip = zip,
                    City = city,
                    KoordX = koordX,
                    KoordY = koordY,
                    StartTime = startTimeToday,
                    EndTime = endTimeToday,
                    IsOpen = isOpen,
                    DistanceAwayKM = distanceAway,
                    Brand = brand,
                    CustomerFlowToday = null,    // You might need to process these later
                    CustomerFlowTomorrow = null, // You might need to process these later
                    ProductList = productList,          // You might need to process this later
                    Id = id
                )

                storeList.add(storeObject)
            }
        }

        return storeList
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
        var categoryEnglish = ""
        var categoryDanish = ""
        try{
            val categories = productDetails.getJSONObject("categories")
            categoryEnglish = if (categories.has("en")) categories.getString("en") else ""
            categoryDanish = if (categories.has("da")) categories.getString("da") else ""
        }catch(e: Exception){
        }



        // Creating and returning the Product object
        return Product(
            name, pictureLink, categoryEnglish, categoryDanish, discountKrones,
            discountPercent, originalPrice, stockLeft, startTime, endTime, newPrice, storeName
        )
    }

}