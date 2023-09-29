package com.example.objects

import java.util.Date

data class Store(
    var Name: String,
    var StreetName: String,
    var Zip: String,
    var City: String,
    var KoordX: Float,
    var KoordY: Float,
    var StartTime: String,
    var EndTime: String,
    var IsOpen: Boolean,
    var ProductList: MutableList<Product>?,
    var DistanceAwayKM: Float,
    var Brand: String,
    var CustomerFlowToday: List<Double>? = listOf(),
    var CustomerFlowTomorrow: List<Double>? = listOf(),
    var Id: String,
) {
    constructor(
        name: String,
        streetName: String,
        zip: String,
        city: String,
        koordX: Float,  // Corrected type to Float
        koordY: Float,  // Corrected type to Float
        startTime: String,
        endTime: String,
        isOpen: Boolean,
        distanceAwayKm: Float,
        brand: String,
        customerFlowToday: List<Double> = listOf(),
        customerFlowTomorrow: List<Double> = listOf(),
        id: String
    ) : this(
        name,
        streetName,
        zip,
        city,
        koordX,
        koordY,
        startTime,
        endTime,
        isOpen,
        mutableListOf(),
        distanceAwayKm,
        brand,
        customerFlowToday,
        customerFlowTomorrow,
        id
    )
}
