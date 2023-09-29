package com.example.objects

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Product(
    var name: String,
    var pictureLink: String,
    var categoryEnglish: String,
    var categoryDanish: String,
    var discountKrones: Int,
    var discountPercent: Int,
    var originalPrice: Int,
    var stockLeft: Int,
    var startTime: Date,
    var endTime: Date,
    var newPrice: Int,
    var storeName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        Date(parcel.readLong()),  // Assuming Date is serialized as a long timestamp
        Date(parcel.readLong()),  // Same assumption as above
        parcel.readInt(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(pictureLink)
        parcel.writeString(categoryEnglish)
        parcel.writeString(categoryDanish)
        parcel.writeInt(discountKrones)
        parcel.writeInt(discountPercent)
        parcel.writeInt(originalPrice)
        parcel.writeInt(stockLeft)
        parcel.writeLong(startTime.time)  // Writing Date as long timestamp
        parcel.writeLong(endTime.time)    // Same as above
        parcel.writeInt(newPrice)
        parcel.writeString(storeName)
    }

    override fun describeContents(): Int {
        return 0
    }

    // ... The rest of your code ...

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }
}
