package com.project.trackproduct.productdetails


import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI

import com.project.trackproduct.R
import com.project.trackproduct.database.ProductDatabase
import com.project.trackproduct.databinding.FragmentProductDetailsBinding
import java.io.FileOutputStream

/**
 * @author Rita Okonkwo
 */
private const val SELECT_IMAGE = 111

class ProductDetailsFragment : Fragment() {

    lateinit var productDetailsViewModel: ProductDetailsViewModel
    lateinit var productDetailsBinding: FragmentProductDetailsBinding
    lateinit var args: ProductDetailsFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        //if argument from list is clicked
        args = ProductDetailsFragmentArgs.fromBundle(requireArguments())
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
            decreaseQty()
        }

        //set on click listener for increase button
        productDetailsBinding.increaseBtn.setOnClickListener {
            increaseQty()
        }

        //get image button on click listener
        productDetailsBinding.uploadImage.setOnClickListener {
            uploadImage()
        }

        //save event observer
        productDetailsViewModel.saveEvent.observe(viewLifecycleOwner, Observer { saveEvent ->
            if (saveEvent) {
                Toast.makeText(activity, "Product saved", Toast.LENGTH_SHORT).show()
                //navigate back to list screen
                this.findNavController()
                    .navigate(ProductDetailsFragmentDirections.actionProductDetailToProductListFragment())
                productDetailsViewModel.doneSaving()
            }
        })

        //delete event observer
        productDetailsViewModel.deleteEvent.observe(viewLifecycleOwner, Observer { deleteEvent ->
            if (deleteEvent) {
                Toast.makeText(activity, "Product Deleted", Toast.LENGTH_SHORT).show()
                //navigate back to list screen
                this.findNavController()
                    .navigate(ProductDetailsFragmentDirections.actionProductDetailToProductListFragment())
                productDetailsViewModel.doneDeleting()
            }
        })

        //update event observer
        productDetailsViewModel.updateEvent.observe(viewLifecycleOwner, Observer { updateEvent ->
            if (updateEvent) {
                Toast.makeText(activity, "Product updated", Toast.LENGTH_SHORT).show()
                //navigate back to list screen
                this.findNavController()
                    .navigate(ProductDetailsFragmentDirections.actionProductDetailToProductListFragment())
                productDetailsViewModel.doneUpdating()
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
                productDetailsBinding.qtyValue.setText(it.productQuantity.toString())
                productDetailsViewModel.productQty.value = it.productQuantity
                productDetailsBinding.supplierInformation.setText(it.supplierInformation)
                productDetailsViewModel.currentPhotoPath.value = it.productImage
                val myBitmap = BitmapFactory.decodeFile(it.productImage)
                productDetailsBinding.productImage.setImageBitmap(myBitmap)
            }
        })

        //order button on click listener
        productDetailsBinding.orderProduct.setOnClickListener {
            validate()
            if (Patterns.EMAIL_ADDRESS.matcher(productDetailsBinding.supplierInformation.text.toString()).matches()) {
                sendEmail()
            } else {
                dialPhone()
            }
        }


        return productDetailsBinding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SELECT_IMAGE && data != null) {
            productDetailsViewModel.setPic(requireContext(), data.data)
                val outputStream =
                    FileOutputStream(productDetailsViewModel.currentPhotoPath.value!!)
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
            if (TextUtils.isEmpty(name)) {
                productDetailsBinding.nameOfProduct.error = "Enter name of product"
                productDetailsBinding.nameOfProduct.requestFocus()
                return false
            }
            if (TextUtils.isEmpty(price)) {
                productDetailsBinding.priceOfProduct.error = "Enter price of product"
                productDetailsBinding.priceOfProduct.requestFocus()
                return false
            }
            if (TextUtils.isEmpty(supplierInfo) || ((!Patterns.EMAIL_ADDRESS.matcher(productDetailsBinding.supplierInformation.text.toString()).matches()) && (!Patterns.PHONE.matcher(productDetailsBinding.supplierInformation.text.toString()).matches()))) {
                productDetailsBinding.supplierInformation.error = "Enter supplier information"
                productDetailsBinding.supplierInformation.requestFocus()
                return false
            }
            if (productDetailsBinding.qtyValue.text.toString() == "0"){
                Toast.makeText(context, "Please enter a quantity for product", Toast.LENGTH_SHORT).show()
                return false
            }
            if(productDetailsBinding.productImage.drawable == null){
                Toast.makeText(context, "Please upload an image", Toast.LENGTH_SHORT).show()
                return false
            }
            if (args.productId == 0L) {
                saveToProduct()
                productDetailsViewModel.saveProduct()
            } else {
                saveToProduct()
                productDetailsViewModel.updateProduct()
            }
        }
        if (item.itemId == R.id.delete) {
            if (args.productId != 0L) {
                buildDialog()
            }
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        )
                || super.onOptionsItemSelected(item)
    }

    fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply {
            putExtra(Intent.EXTRA_SUBJECT, productDetailsBinding.nameOfProduct.text.toString())
            putExtra(
                Intent.EXTRA_EMAIL,
                arrayOf(productDetailsBinding.supplierInformation.text.toString())
            )
        }
        startActivity(intent)
    }

    fun dialPhone() {
        val uri = Uri.parse("tel:" + productDetailsBinding.supplierInformation.text.toString())
        val intent1 = Intent(Intent.ACTION_DIAL)
        intent1.data = uri
        startActivity(intent1)
    }

    fun increaseQty() {
        productDetailsViewModel.increaseQty()
        productDetailsBinding.qtyValue.setText(productDetailsViewModel.productQty.value.toString())
    }

    fun decreaseQty() {
        productDetailsViewModel.decreaseQty()
        productDetailsBinding.qtyValue.setText(productDetailsViewModel.productQty.value.toString())
    }

    fun saveToProduct() {
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

    fun uploadImage() {
        val photoIntent = Intent()
        photoIntent.type = "image/*"
        photoIntent.action = Intent.ACTION_GET_CONTENT
        productDetailsViewModel.createImageFile(requireContext())
        startActivityForResult(
            Intent.createChooser(photoIntent, "Select Picture"),
            SELECT_IMAGE
        )
    }

    fun validate() {
        val name = productDetailsBinding.nameOfProduct.text.toString()
        val price = productDetailsBinding.priceOfProduct.text.toString()
        val supplierInfo = productDetailsBinding.supplierInformation.text.toString()
        if (TextUtils.isEmpty(name)) {
            productDetailsBinding.nameOfProduct.error = "Enter name of product"
            productDetailsBinding.nameOfProduct.requestFocus()
            return
        }
        if (TextUtils.isEmpty(price)) {
            productDetailsBinding.priceOfProduct.error = "Enter price of product"
            productDetailsBinding.priceOfProduct.requestFocus()
            return
        }
        if (TextUtils.isEmpty(supplierInfo)) {
            productDetailsBinding.supplierInformation.error = "Enter supplier information"
            productDetailsBinding.supplierInformation.requestFocus()
            return
        }
    }

    fun buildDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Product")
        builder.setMessage("Are you sure?")
        builder.setNegativeButton("Cancel") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.setPositiveButton("Delete") { dialogInterface, i ->
            productDetailsViewModel.deleteProduct()
        }
        builder.show()
    }


}
