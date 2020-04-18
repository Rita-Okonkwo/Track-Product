package com.project.trackproduct.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "product_table")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    var productId : Long = 0L,

    @ColumnInfo(name = "Name")
    var productName : String = "",

    @ColumnInfo(name = "Price")
    var productPrice : Int = 0,

    @ColumnInfo(name = "Quantity")
    var productQuantity : Int = 0,

    @ColumnInfo(name = "Supplier Information")
    var supplierInformation : String = "",

    @ColumnInfo(name = "Product Image")
    var productImage : String = ""
)