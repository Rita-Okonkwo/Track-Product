package com.project.trackproduct.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ProductDao{
    @Insert
    fun insert(product: ProductEntity)

    @Update
    fun update(product: ProductEntity)

    @Query("SELECT * FROM product_table WHERE productId = :key")
    fun get(key: Long): ProductEntity?

    @Query("SELECT * FROM product_table ORDER BY productId DESC")
    fun getAllProducts(): LiveData<List<ProductEntity>>

    @Delete
    fun delete(product: ProductEntity)
}