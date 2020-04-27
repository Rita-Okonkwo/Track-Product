package com.project.trackproduct.productdetails


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.databinding.DataBindingComponent
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI

import com.project.trackproduct.R
import com.project.trackproduct.database.ProductDatabase
import com.project.trackproduct.databinding.FragmentProductDetailsBinding
import java.io.File
import java.io.FileOutputStream
import java.util.jar.Manifest

/**
 * @author Rita Okonkwo
 */
private const val SELECT_IMAGE = 111

class ProductDetailsFragment : Fragment() {

    lateinit var productDetailsViewModel: ProductDetailsViewModel
    lateinit var productDetailsBinding: FragmentProductDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        //if argument from list is clicked
        val args = ProductDetailsFragmentArgs.fromBundle(requireArguments())
        // Inflate the layout for this fragment
        productDetailsBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_details, container, false)
        val application = requireNotNull(this.activity).application
        val datasource = ProductDatabase.getInstance(application).productDao
        val viewModelFactory = ProductDetailsViewModelFactory(args.productId, datasource)
        productDetailsViewModel =
            ViewModelProviders.of(this, viewModelFactory).get(ProductDetailsViewModel::class.java)

        //set on click listener for decrease button
        productDetailsBinding.decreaseBtn.setOnClickListener {
           decrease_qty()
        }

        //set on click listener for increase button
        productDetailsBinding.increaseBtn.setOnClickListener {
           increase_qty()
        }

        //get image button on click listener
        productDetailsBinding.uploadImage.setOnClickListener {
            upload_image()
        }

        //save event observer
        productDetailsViewModel.saveEvent.observe(viewLifecycleOwner, Observer { saveEvent ->
            if (saveEvent) {
                Toast.makeText(activity, "Product saved", Toast.LENGTH_SHORT).show()
                productDetailsViewModel.doneSaving()
            }
        })

        //display image bitmap in image view
        productDetailsViewModel.myBitmap.observe(viewLifecycleOwner, Observer { myBitmap ->
            productDetailsBinding.productImage.setImageBitmap(myBitmap)
        })

        //get products
        productDetailsViewModel.getProduct()

        //observe the product live data
        productDetailsViewModel.product.observe(viewLifecycleOwner, Observer {
            it?.let {
                productDetailsBinding.nameOfProduct.setText(it.productName)
                productDetailsBinding.priceOfProduct.setText(it.productPrice.toString())
                productDetailsBinding.qtyValue.text = it.productQuantity.toString()
                productDetailsBinding.supplierInformation.setText(it.supplierInformation)
                val myBitmap = BitmapFactory.decodeFile(it.productImage)
                productDetailsBinding.productImage.setImageBitmap(myBitmap)
            }
        })

        //order button on click listener
        productDetailsBinding.orderProduct.setOnClickListener {
            validate()
            if(Patterns.EMAIL_ADDRESS.matcher(productDetailsBinding.supplierInformation.text.toString()).matches()){
               send_email()
            }else{
               dial_phone()
            }
        }


        return productDetailsBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE) {
            productDetailsViewModel.setPic(requireContext(), data?.data)
            val outputStream = FileOutputStream(productDetailsViewModel.currentPhotoPath.value!!)
            productDetailsViewModel.myBitmap.value?.compress(
                Bitmap.CompressFormat.PNG,
                100,
                outputStream
            )
            outputStream.close()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.delete_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            val name = productDetailsBinding.nameOfProduct.text.toString()
            val price = productDetailsBinding.priceOfProduct.text.toString()
            val supplierInfo = productDetailsBinding.supplierInformation.text.toString()
            if(TextUtils.isEmpty(name)){
                productDetailsBinding.nameOfProduct.error = "Enter name of product"
                productDetailsBinding.nameOfProduct.requestFocus()
                return false
            }
            if(TextUtils.isEmpty(price)){
                productDetailsBinding.priceOfProduct.error = "Enter price of product"
                productDetailsBinding.priceOfProduct.requestFocus()
                return false
            }
            if(TextUtils.isEmpty(supplierInfo)){
                productDetailsBinding.supplierInformation.error = "Enter supplier information"
                productDetailsBinding.supplierInformation.requestFocus()
                return false
            }
            saveToProduct()
            productDetailsViewModel.saveProduct()
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        )
                || super.onOptionsItemSelected(item)
    }

    fun send_email(){
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_SUBJECT, productDetailsBinding.nameOfProduct.text.toString())
            putExtra(Intent.EXTRA_EMAIL, arrayOf(productDetailsBinding.supplierInformation.text.toString()))
        }
        startActivity(intent)
    }

    fun dial_phone(){
        val uri = Uri.parse("tel:" + productDetailsBinding.supplierInformation.text.toString())
        val intent1 = Intent(Intent.ACTION_DIAL)
        intent1.data = uri
        startActivity(intent1)
    }

    fun increase_qty(){
        productDetailsViewModel.increaseQty()
        productDetailsBinding.qtyValue.text =
            productDetailsViewModel.productQty.value.toString()
    }

    fun decrease_qty(){
        productDetailsViewModel.decreaseQty()
        productDetailsBinding.qtyValue.text =
            productDetailsViewModel.productQty.value.toString()
    }

    fun saveToProduct(){
        productDetailsViewModel.productName.value =
            productDetailsBinding.nameOfProduct.text.toString()
        productDetailsViewModel.productQty.value =
            productDetailsBinding.qtyValue.text.toString().toInt()
        productDetailsViewModel.productPrice.value =
            productDetailsBinding.priceOfProduct.text.toString()
        productDetailsViewModel.productSupplier.value =
            productDetailsBinding.supplierInformation.text.toString()
        productDetailsViewModel.productImage.value =
            productDetailsViewModel.currentPhotoPath.value
    }

    fun upload_image(){
        val photoIntent = Intent()
        photoIntent.type = "image/*"
        photoIntent.action = Intent.ACTION_GET_CONTENT
        productDetailsViewModel.createImageFile(requireContext())
        startActivityForResult(
            Intent.createChooser(photoIntent, "Select Picture"),
            SELECT_IMAGE
        )
    }

    fun validate(){
        val name = productDetailsBinding.nameOfProduct.text.toString()
        val price = productDetailsBinding.priceOfProduct.text.toString()
        val supplierInfo = productDetailsBinding.supplierInformation.text.toString()
        if(TextUtils.isEmpty(name)){
            productDetailsBinding.nameOfProduct.error = "Enter name of product"
            productDetailsBinding.nameOfProduct.requestFocus()
            return
        }
        if(TextUtils.isEmpty(price)){
            productDetailsBinding.priceOfProduct.error = "Enter price of product"
            productDetailsBinding.priceOfProduct.requestFocus()
            return
        }
        if(TextUtils.isEmpty(supplierInfo)){
            productDetailsBinding.supplierInformation.error = "Enter supplier information"
            productDetailsBinding.supplierInformation.requestFocus()
            return
        }
    }


}
