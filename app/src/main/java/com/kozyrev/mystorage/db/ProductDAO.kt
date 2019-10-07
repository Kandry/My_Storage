package com.kozyrev.mystorage.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface ProductDAO {

    @get:Query("SELECT * FROM PRODUCTS")
    val allProducts: List<Product>

    @get:Query("SELECT * FROM PRODUCTS")
    val allProductsFlowable: Flowable<List<Product>>

    @Insert
    fun insert(product: Product)

    @Insert
    fun insertAll(vararg products: Product)

    @Update
    fun update(product: Product)

    @Delete
    fun delete(product: Product)

    @Query("SELECT * FROM PRODUCTS where id = :productID")
    fun getProductById(productID: Int): Product

    @Query("SELECT * FROM PRODUCTS where id = :productID")
    fun getProductByIdSingle(productID: Int): Single<Product>
}