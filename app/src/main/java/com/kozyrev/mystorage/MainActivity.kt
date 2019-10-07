package com.kozyrev.mystorage

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.room.Room

import android.content.Intent
import android.view.View

import com.kozyrev.mystorage.adapter.ProductsAdapter
import com.kozyrev.mystorage.db.Product
import com.kozyrev.mystorage.db.ProductDB

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subscribers.DisposableSubscriber

class MainActivity : AppCompatActivity() {

    private var productsView: RecyclerView? = null
    private var products: List<Product>? = null
    private var db: ProductDB? = null

    private var toolbar: Toolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initDB()
    }

    override fun onStart() {
        super.onStart()
        downloadAllProducts()
    }

    fun addProduct(view: View) {
        val intent = Intent(this@MainActivity, DetailProductActivity::class.java)
        startActivity(intent)
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        productsView = findViewById(R.id.productsRecycler)
        val layoutManager = StaggeredGridLayoutManager(1, 1)
        productsView!!.layoutManager = layoutManager
        productsView!!.itemAnimator = DefaultItemAnimator()
        productsView!!.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    private fun initDB() {
        db = Room.databaseBuilder<ProductDB>(applicationContext, ProductDB::class.java, "productdatabase")
            .allowMainThreadQueries()
            .build()
    }

    private fun downloadAllProducts() {
        val allProducts = db!!.productDAO.allProductsFlowable
        allProducts
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : DisposableSubscriber<List<Product>>() {
                override fun onNext(productsList: List<Product>) {
                    products = productsList
                    if (productsView!!.adapter != null) {
                        updateAdapter()
                    } else {
                        createAdapter()
                    }
                }

                override fun onError(t: Throwable) {}

                override fun onComplete() {}
            })
    }

    private fun createAdapter() {
        val adapter = ProductsAdapter(products)
        productsView!!.adapter = adapter
        adapter.setListener(object : ProductsAdapter.ProductsListener {
            override fun onClick(position: Int) {
                val intent = Intent(this@MainActivity, DetailProductActivity::class.java)
                intent.putExtra(DetailProductActivity.EXTRA_PRODUCT_ID, products!![position].id)
                this@MainActivity.startActivity(intent)
            }
        })
    }

    private fun updateAdapter() {
        val adapter = productsView!!.adapter as ProductsAdapter?
        adapter?.notifyUpdateProducts(products!!)
    }
}
