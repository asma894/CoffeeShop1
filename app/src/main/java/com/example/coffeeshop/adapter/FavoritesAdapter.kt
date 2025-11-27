package com.example.coffeeshop.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions
import com.example.coffeeshop.R
import com.example.coffeeshop.activity.DetailedActivity
import com.example.coffeeshop.databinding.ViewholderFavoritesBinding
import com.example.coffeeshop.helper.ManagementFavorites
import com.example.coffeeshop.model.ItemsModel

class FavoritesAdapter(
    private val items: ArrayList<ItemsModel>,
    private val context: Context,
    private val onItemRemoved: () -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.ViewHolder>() {

    private val managementFavorites = ManagementFavorites(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ViewholderFavoritesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.binding.titleTxt.text = item.title
        holder.binding.priceTxt.text = "$${item.price}"
        holder.binding.ratingBar.rating = item.rating.toFloat()
        holder.binding.descriptionTxt.text = item.description

        Glide.with(holder.itemView.context)
            .load(item.picUrl[0])
            .apply(RequestOptions().transform(CenterCrop()))
            .into(holder.binding.itemImage)

        // Remove from favorites
        holder.binding.removeFavBtn.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                managementFavorites.removeFavorite(item)
                items.removeAt(currentPosition)
                notifyItemRemoved(currentPosition)
                notifyItemRangeChanged(currentPosition, items.size)
                onItemRemoved()
            }
        }

        // Click to view details
        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailedActivity::class.java)
            intent.putExtra("object", item)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(val binding: ViewholderFavoritesBinding) :
        RecyclerView.ViewHolder(binding.root)
}