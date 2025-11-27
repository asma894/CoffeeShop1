package com.example.coffeeshop.activity

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.adapter.FavoritesAdapter
import com.example.coffeeshop.databinding.ActivityFavoritesBinding
import com.example.coffeeshop.helper.ManagementFavorites
import com.example.coffeeshop.model.ItemsModel

class FavoritesActivity : BaseActivity() {

    private val binding: ActivityFavoritesBinding by lazy {
        ActivityFavoritesBinding.inflate(layoutInflater)
    }

    private lateinit var managementFavorites: ManagementFavorites
    private lateinit var adapter: FavoritesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        managementFavorites = ManagementFavorites(this)

        setupViews()
        loadFavorites()
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadFavorites() {
        val favoritesList = managementFavorites.getListFavorites()

        if (favoritesList.isEmpty()) {
            binding.emptyView.visibility = View.VISIBLE
            binding.favoritesRecyclerView.visibility = View.GONE
        } else {
            binding.emptyView.visibility = View.GONE
            binding.favoritesRecyclerView.visibility = View.VISIBLE

            binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )

            adapter = FavoritesAdapter(favoritesList, this) {
                // Callback when item is removed
                loadFavorites() // Refresh the list
            }
            binding.favoritesRecyclerView.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites() // Refresh when returning to activity
    }
}