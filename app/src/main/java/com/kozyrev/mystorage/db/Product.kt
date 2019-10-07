package com.kozyrev.mystorage.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "PRODUCTS")
class Product(var name: String?, var price: String?, var currency: String?, var imageResource: String?) {

    @PrimaryKey(autoGenerate = true)
    var id: Int = 0
}