package com.project.trackproduct.productlist


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

import com.project.trackproduct.R

/**
 * A simple [Fragment] subclass.
 */
class ProductListFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_product_list, container, false)
        val recyclerView: RecyclerView = view.findViewById(R.id.product_recycler)
        recyclerView.layoutManager = LinearLayoutManager(activity)

        val addProductButton : FloatingActionButton = view.findViewById(R.id.addProduct)
        addProductButton.setOnClickListener {
            view.findNavController().navigate(ProductListFragmentDirections.actionProductListFragmentToProductDetailFragment())
        }
        return view

    }


}
