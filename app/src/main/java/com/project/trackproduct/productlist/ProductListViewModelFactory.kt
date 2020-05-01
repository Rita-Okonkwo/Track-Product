package com.project.trackproduct.productlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.trackproduct.database.ProductDao
import com.project.trackproduct.productdetails.ProductDetailsViewModel

class ProductListViewModelFactory(val database: ProductDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductListViewModel::class.java)) {
            return ProductListViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}