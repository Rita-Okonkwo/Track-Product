package com.project.trackproduct.productdetails

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.project.trackproduct.database.ProductDao
import com.project.trackproduct.database.ProductEntity
import kotlinx.coroutines.*
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

class ProductDetailsViewModel(val productKey: Long, val database: ProductDao) : ViewModel() {
    var currentPhotoPath = MutableLiveData<String?>()
    var myBitmap = MutableLiveData<Bitmap>()
    var productName = MutableLiveData<String>()
    var productPrice = MutableLiveData<String>()
    var productQty = MutableLiveData<Int>()
    var productSupplier = MutableLiveData<String>()
    var productImage = MutableLiveData<String>()
    var product = MutableLiveData<ProductEntity?>()
    private val _saveEvent = MutableLiveData<Boolean>()
    val saveEvent: LiveData<Boolean>
        get() = _saveEvent
    private val _updateEvent = MutableLiveData<Boolean>()
    val updateEvent: LiveData<Boolean>
        get() = _updateEvent
    private val _deleteEvent = MutableLiveData<Boolean>()
    val deleteEvent: LiveData<Boolean>
        get() = _deleteEvent

    init {
        productName.value = ""
        productPrice.value = ""
        productQty.value = 0
        productSupplier.value = ""
        productImage.value = ""
        _saveEvent.value = false
        product.value = null
    }

    private var viewModelJob = Job()
    private var uiCoroutineScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun decreaseQty() {
        val newValue = productQty.value
        if (newValue!! > 0) {
            productQty.value = newValue.minus(1)
        }
    }

    fun increaseQty() {
        val qtyValue = productQty.value
        productQty.value = qtyValue!!.plus(1)
    }

    fun saveProduct() {
        val product = ProductEntity()
        product.productName = productName.value!!
        product.productPrice = productPrice.value!!.toLong()
        product.productQuantity = productQty.value!!
        product.supplierInformation = productSupplier.value!!
        product.productImage = productImage.value!!
        uiCoroutineScope.launch {
            insert(product)
            _saveEvent.value = true
        }
    }

    private suspend fun insert(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            database.insert(product)
        }
    }

    fun doneSaving() {
        _saveEvent.value = false
    }

    fun doneUpdating() {
        _updateEvent.value = false
    }

    fun doneDeleting() {
        _deleteEvent.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    @Throws(IOException::class)
    fun createImageFile(context: Context): File {
        // Create an image file name
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath.value = absolutePath
        }
    }

    fun getProduct() {
        uiCoroutineScope.launch {
            product.value = get()
        }
    }

    private suspend fun get(): ProductEntity? {
        return withContext(Dispatchers.IO) {
            database.get(productKey)
        }
    }

    fun updateProduct() {
        val product = product.value!!
        product.productName = productName.value!!
        product.productPrice = productPrice.value!!.toLong()
        product.productQuantity = productQty.value!!
        product.supplierInformation = productSupplier.value!!
        product.productImage = productImage.value!!
        uiCoroutineScope.launch {
            update(product)
            _updateEvent.value = true
        }
    }

    private suspend fun update(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            database.update(product)
        }
    }

    fun deleteProduct() {
        uiCoroutineScope.launch {
            delete(product.value!!)
            _deleteEvent.value = true
        }
    }

    private suspend fun delete(product: ProductEntity) {
        withContext(Dispatchers.IO) {
            database.delete(product)
        }
    }


    @Throws(FileNotFoundException::class)
    fun setPic(context: Context, uri: Uri?) {
        // Get the dimensions of the View
        val targetW: Int = 100
        val targetH: Int = 100

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true
            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.min(photoW / targetW, photoH / targetH)

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        myBitmap.value = BitmapFactory.decodeStream(
            context.contentResolver
                .openInputStream(uri!!), null, bmOptions
        )
    }

}