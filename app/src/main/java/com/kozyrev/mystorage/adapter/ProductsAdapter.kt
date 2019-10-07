package com.kozyrev.mystorage.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

import com.kozyrev.mystorage.db.Product
import com.kozyrev.mystorage.R
import com.squareup.picasso.Picasso

internal class ProductsAdapter(private var products: List<Product>?) : RecyclerView.Adapter<ProductsAdapter.ViewHolder>() {
    private var listener: ProductsListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cardView = LayoutInflater.from(parent.context).inflate(R.layout.card_product, parent, false) as CardView
        val viewHolder = ViewHolder(cardView)

        cardView.setOnClickListener {
            val adapterPosition = viewHolder.adapterPosition
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener!!.onClick(adapterPosition)
            }
        }

        return viewHolder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView
        val product = products!![position]

        textViewSetText(cardView, R.id.cardviewTitle, product.name)
        textViewSetText(cardView, R.id.cardviewPrice, product.price)

        if (product.currency != "") {
            var currencyResourceId = 0
            when (product.currency) {
                "EUR" -> currencyResourceId = R.drawable.euro_currency_symbol
                "USD" -> currencyResourceId = R.drawable.dollar_currency_symbol
                "RUB" -> currencyResourceId = R.drawable.iconfinder_ruble_1608820
            }

            if (currencyResourceId != 0) {
                val cardviewCurrency = cardView.findViewById<ImageView>(R.id.cardviewCurrency)
                Picasso.get()
                    .load(currencyResourceId)
                    .fit()
                    .centerCrop()
                    .into(cardviewCurrency)
            }

        }

        val imageView = cardView.findViewById<ImageView>(R.id.cardviewImage)
        imageView.setImageURI(null)

        if (product.imageResource != null) {
            val imageUri = Uri.parse(product.imageResource)
            Picasso.get()
                .load(imageUri)
                .fit()
                .centerCrop()
                .into(imageView)
            imageView.contentDescription = product.name
        }
    }

    override fun getItemCount(): Int {
        return if (products == null) 0 else products!!.size
    }

    fun notifyUpdateProducts(products: List<Product>) {
        this.products = products
        this.notifyDataSetChanged()
    }

    private fun textViewSetText(cardView: CardView, textViewId: Int, text: String?) {
        val textView = cardView.findViewById<TextView>(textViewId)
        textView.text = text
    }

    fun setListener(listener: ProductsListener) {
        this.listener = listener
    }

    interface ProductsListener {
        fun onClick(position: Int)
    }


    internal inner class ViewHolder(val cardView: CardView) : RecyclerView.ViewHolder(cardView)
}