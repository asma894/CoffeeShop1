package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.coffeeshop.R
import com.example.coffeeshop.adapter.CategoryAdapter
import com.example.coffeeshop.adapter.OffersAdapter
import com.example.coffeeshop.adapter.PopularAdapter
import com.example.coffeeshop.databinding.ActivityMainBinding
import com.example.coffeeshop.model.ItemsModel
import com.example.coffeeshop.viewmodel.MainViewModel
import com.google.firebase.auth.FirebaseAuth

class MainActivity : BaseActivity() {

    private val viewModel = MainViewModel()
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private var allItems: MutableList<ItemsModel> = mutableListOf()
    private var filteredItems: MutableList<ItemsModel> = mutableListOf()
    private var selectedCategory: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.bottomNavigation.background = null

        initCategory()
        initPopular()
        initOffer()
        bottomMenu()
        setupSearch()
        setupSeeAll()
    }

    private fun setupSeeAll() {
        binding.seeAllBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, AllProductsActivity::class.java))
        }
    }

    private fun setupSearch() {
        binding.editTextText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterItems(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterItems(query: String) {
        filteredItems.clear()

        if (query.isEmpty() && selectedCategory.isEmpty()) {
            filteredItems.addAll(allItems)
        } else {
            for (item in allItems) {
                val matchesQuery = query.isEmpty() ||
                        item.title.contains(query, ignoreCase = true) ||
                        item.description.contains(query, ignoreCase = true)

                val matchesCategory = selectedCategory.isEmpty() ||
                        item.title.contains(selectedCategory, ignoreCase = true)

                if (matchesQuery && matchesCategory) {
                    filteredItems.add(item)
                }
            }
        }

        updatePopularAdapter()
    }

    private fun updatePopularAdapter() {
        binding.recyclerViewPopular.adapter = PopularAdapter(filteredItems)

        if (filteredItems.isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.recyclerViewPopular.visibility = View.GONE
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.recyclerViewPopular.visibility = View.VISIBLE
        }
    }

    private fun bottomMenu() {
        binding.cartBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, CartActivity::class.java))
        }

        binding.bottomNavigation.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    true
                }
                R.id.profile -> {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser != null) {
                        startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
                    } else {
                        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
                    }
                    true
                }
                R.id.favorites -> {
                    startActivity(Intent(this@MainActivity, FavoritesActivity::class.java))
                    true
                }
                R.id.settings -> {
                    startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

    private fun initOffer() {
        binding.progressBarOffer.visibility = View.VISIBLE
        viewModel.offer.observe(this, Observer {
            binding.recyclerViewOffer.layoutManager =
                LinearLayoutManager(this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewOffer.adapter = OffersAdapter(it)
            binding.progressBarOffer.visibility = View.GONE
        })
        viewModel.loadOffer()
    }

    private fun initPopular() {
        binding.progressBarPopular.visibility = View.VISIBLE
        viewModel.popular.observe(this, Observer {
            allItems = it
            filteredItems.clear()
            filteredItems.addAll(it)

            binding.recyclerViewPopular.layoutManager =
                LinearLayoutManager(this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewPopular.adapter = PopularAdapter(filteredItems)
            binding.progressBarPopular.visibility = View.GONE
        })
        viewModel.loadPopular()
    }

    private fun initCategory() {
        binding.progressBarCategory.visibility = View.VISIBLE
        viewModel.category.observe(this, Observer {
            binding.recyclerViewCategory.layoutManager =
                LinearLayoutManager(this@MainActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
            binding.recyclerViewCategory.adapter = CategoryAdapter(it) { category ->
                selectedCategory = if (selectedCategory == category) "" else category
                filterItems(binding.editTextText.text.toString())
            }
            binding.progressBarCategory.visibility = View.GONE
        })
        viewModel.loadCategory()
    }
}