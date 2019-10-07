package com.kozyrev.mystorage.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Product::class], version = 1)
abstract class ProductDB : RoomDatabase() {
    abstract val productDAO: ProductDAO
}