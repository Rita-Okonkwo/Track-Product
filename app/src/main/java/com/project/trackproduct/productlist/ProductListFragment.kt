package com.project.trackproduct.productlist


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.project.trackproduct.R
import com.project.trackproduct.databinding.FragmentProductDetailsBinding
import com.project.trackproduct.databinding.FragmentProductListBinding

/**
 * A simple [Fragment] subclass.
 */
class ProductListFragment : Fragment() {

    lateinit var productListBinding: FragmentProductListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        productListBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_product_list, container, false)


        productListBinding.addProduct.setOnClickListener {
            view?.findNavController()
                ?.navigate(ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment())
        }
        return productListBinding.root

    }


}
