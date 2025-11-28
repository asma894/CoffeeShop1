package com.example.coffeeshop.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.coffeeshop.R
import com.example.coffeeshop.activity.DetailedActivity
import com.example.coffeeshop.databinding.ItemProductGridBinding
import com.example.coffeeshop.model.ItemsModel

class ProductGridAdapter(
    private val items: MutableList<ItemsModel>,
    private val context: Context
) : RecyclerView.Adapter<ProductGridAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemProductGridBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemProductGridBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.apply {
            titleTxt.text = item.title
            priceTxt.text = "$${item.price}"
            ratingBar.rating = item.rating.toFloat()
            ratingTxt.text = item.rating.toString()

            Glide.with(context)
                .load(item.picUrl[0])
                .apply(RequestOptions.bitmapTransform(RoundedCorners(30)))
                .into(productImg)

            root.setOnClickListener {
                val intent = Intent(context, DetailedActivity::class.java)
                intent.putExtra("object", item)
                context.startActivity(intent)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}