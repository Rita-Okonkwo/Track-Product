package com.project.trackproduct.productdetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.trackproduct.database.ProductDao
import com.project.trackproduct.database.ProductDatabase

class ProductDetailsViewModelFactory(val productKey: Long, val database: ProductDao) :
    ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductDetailsViewModel::class.java)) {
            return ProductDetailsViewModel(productKey, database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
