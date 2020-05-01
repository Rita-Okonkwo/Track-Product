package com.project.trackproduct.productlist

import androidx.lifecycle.ViewModel
import com.project.trackproduct.database.ProductDao

class ProductListViewModel(val database: ProductDao) : ViewModel() {
    //initialize nights in database
    val products = database.getAllProducts()

}