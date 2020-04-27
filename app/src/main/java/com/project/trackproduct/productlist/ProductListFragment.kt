package com.project.trackproduct.productlist


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.project.trackproduct.R
import com.project.trackproduct.database.ProductDatabase
import com.project.trackproduct.databinding.FragmentProductDetailsBinding
import com.project.trackproduct.databinding.FragmentProductListBinding
import com.project.trackproduct.productdetails.ProductDetailsViewModelFactory

/**
 * A simple [Fragment] subclass.
 */
class ProductListFragment : Fragment() {

    private lateinit var productListBinding: FragmentProductListBinding
    private lateinit var productListViewModel: ProductListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        productListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_list, container, false)

        val application = requireNotNull(this.activity).application
        //initialize database
        val datasource = ProductDatabase.getInstance(application).productDao
        //initialize view model factory
        val viewModelFactory = ProductListViewModelFactory(datasource)
        //initialize viewmodel
        productListViewModel = ViewModelProviders.of(this, viewModelFactory).get(ProductListViewModel::class.java)

        //FAB button for adding new product
        productListBinding.addProduct.setOnClickListener {
            view?.findNavController()
                ?.navigate(ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment())
        }
        val productListAdapter = ProductListAdapter(ProductListAdapter.ProductListener{
            productId ->
            view?.findNavController()?.navigate(ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment(productId))
        })
        productListBinding.productRecycler.adapter = productListAdapter
        productListBinding.productRecycler.layoutManager = LinearLayoutManager(activity)

        productListViewModel.products.observe(viewLifecycleOwner, Observer {
            it?.let {
                productListAdapter.submitList(it)
            }
        })
        return productListBinding.root

    }


}
