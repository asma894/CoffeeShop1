package com.example.coffeeshop.helper

import android.content.Context
import android.widget.Toast
import com.example.coffeeshop.model.ItemsModel

class ManagementFavorites(val context: Context) {

    private val tinyDB = TinyDB(context)

    fun insertFavorite(item: ItemsModel) {
        var listItem = getListFavorites()
        val existAlready = listItem.any { it.title == item.title }

        if (existAlready) {
            Toast.makeText(context, "Already in favorites", Toast.LENGTH_SHORT).show()
        } else {
            listItem.add(item)
            tinyDB.putListObject("FavoritesList", listItem)
            Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeFavorite(item: ItemsModel) {
        var listItem = getListFavorites()
        listItem.removeAll { it.title == item.title }
        tinyDB.putListObject("FavoritesList", listItem)
        Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show()
    }

    fun isFavorite(item: ItemsModel): Boolean {
        val listItem = getListFavorites()
        return listItem.any { it.title == item.title }
    }

    fun getListFavorites(): ArrayList<ItemsModel> {
        return tinyDB.getListObject("FavoritesList") ?: arrayListOf()
    }

    fun toggleFavorite(item: ItemsModel): Boolean {
        return if (isFavorite(item)) {
            removeFavorite(item)
            false
        } else {
            insertFavorite(item)
            true
        }
    }
}