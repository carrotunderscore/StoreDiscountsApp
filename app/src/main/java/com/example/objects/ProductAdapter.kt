package com.example.objects

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storediscount.R
import com.squareup.picasso.Picasso

class ProductAdapter(private val productList: MutableList<Product>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val storeName: TextView = itemView.findViewById(R.id.address)
        val newPrice: TextView = itemView.findViewById(R.id.newPrice)
        val originalPrice: TextView = itemView.findViewById(R.id.originalPrice)
        val discountPercent: TextView = itemView.findViewById(R.id.discountPercent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.product_card, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = productList[position]
        holder.productName.text = product.name
        holder.storeName.text = product.storeName
        Log.d("ABC", "product.storeName: ${product.storeName}")
        Log.d("ABC", "product.name: ${product.name}")
        holder.newPrice.text = "Price: " + product.newPrice.toString() + " DKK"
        holder.originalPrice.text = "Old price: " +product.originalPrice.toString() + " DKK"
        holder.discountPercent.text = product.discountPercent.toString() + "%"
        Glide.with(holder.productImage.context)
            .load(product.pictureLink)
            .into(holder.productImage)
        Log.d("ABC", "HEJ")
        holder.productImage.setOnClickListener {
            // Create and show the dialog
            val builder = AlertDialog.Builder(it.context)
            val inflater = LayoutInflater.from(it.context)
            val view = inflater.inflate(R.layout.dialog_image, null)
            val fullImageView: ImageView = view.findViewById(R.id.fullImageView)
            Log.d("ABC", "Image URL: ${product.pictureLink}")

            // Use Picasso to load the image
            Picasso.get()
                .load(product.pictureLink)
                .into(fullImageView)

            builder.setView(view)
            val dialog = builder.create()

            // If you want the dialog to be dismissible by clicking outside
            dialog.setCancelable(true)
            dialog.show()
        }
        holder.storeName.setOnClickListener {
            // Get the text from the TextView

            // Obtain the Context from the ViewHolder's itemView
            val context = holder.storeName.context

            // Get the ClipboardManager system service
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Create a ClipData object to hold the text
            val clipData = ClipData.newPlainText("TextView Text", holder.storeName.text)

            // Set the ClipData to the clipboard
            clipboardManager.setPrimaryClip(clipData)

            // Use the obtained Context to display a Toast message
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

        }

        holder.productName.setOnClickListener {
            // Get the text from the TextView

            // Obtain the Context from the ViewHolder's itemView
            val context = holder.productName.context

            // Get the ClipboardManager system service
            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Create a ClipData object to hold the text
            val clipData = ClipData.newPlainText("TextView Text", holder.productName.text)

            // Set the ClipData to the clipboard
            clipboardManager.setPrimaryClip(clipData)

            // Use the obtained Context to display a Toast message
            Toast.makeText(context, "Text copied to clipboard", Toast.LENGTH_SHORT).show()

        }

    }



    override fun getItemCount() = productList.size
}