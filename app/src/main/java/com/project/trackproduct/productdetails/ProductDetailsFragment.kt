package com.project.trackproduct.productdetails


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
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
            productDetailsViewModel.decreaseQty()
            productDetailsBinding.qtyValue.text =
                productDetailsViewModel.productQty.value.toString()
        }

        //set on click listener for increase button
        productDetailsBinding.increaseBtn.setOnClickListener {
            productDetailsViewModel.increaseQty()
            productDetailsBinding.qtyValue.text =
                productDetailsViewModel.productQty.value.toString()
        }

        //get image button on click listener
        productDetailsBinding.uploadImage.setOnClickListener {
            val photoIntent = Intent()
            photoIntent.type = "image/*"
            photoIntent.action = Intent.ACTION_GET_CONTENT
            productDetailsViewModel.createImageFile(requireContext())
            startActivityForResult(
                Intent.createChooser(photoIntent, "Select Picture"),
                SELECT_IMAGE
            )
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
            saveToProduct()
            productDetailsViewModel.saveProduct()
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        )
                || super.onOptionsItemSelected(item)
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


}
