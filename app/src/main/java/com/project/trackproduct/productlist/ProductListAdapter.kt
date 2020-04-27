package com.project.trackproduct.productlist

import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.project.trackproduct.database.ProductEntity
import com.project.trackproduct.databinding.ProductListItemBinding

class ProductListAdapter(val clickListener: ProductListener) : ListAdapter<ProductEntity, ProductListAdapter.ViewHolder>(ProductListDiffCallback()){
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProductListAdapter.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: ProductListAdapter.ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(clickListener, item)
    }

    class ViewHolder private constructor(val binding: ProductListItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(clickListener: ProductListener, item : ProductEntity){
            binding.nameText.text = item.productName
            binding.priceTxt.text = item.productPrice.toString()
            binding.qtyText.text = item.productQuantity.toString()
            val myBitmap = BitmapFactory.decodeFile(item.productImage)
            binding.productImage.setImageBitmap(myBitmap)
            binding.clickListener = clickListener
            binding.product = item
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ProductListItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    class ProductListDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }

    class ProductListener(val clickListener: (productId: Long) -> Unit) {
        fun onClick(product: ProductEntity) = clickListener(product.productId)
    }

}