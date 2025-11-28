package com.example.coffeeshop.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coffeeshop.adapter.CategoryAdapter
import com.example.coffeeshop.adapter.ProductGridAdapter
import com.example.coffeeshop.databinding.ActivityAllProductsBinding
import com.example.coffeeshop.model.ItemsModel
import com.example.coffeeshop.viewmodel.MainViewModel

class AllProductsActivity : BaseActivity() {

    private val binding: ActivityAllProductsBinding by lazy {
        ActivityAllProductsBinding.inflate(layoutInflater)
    }

    private val viewModel = MainViewModel()
    private var allProducts: MutableList<ItemsModel> = mutableListOf()
    private var filteredProducts: MutableList<ItemsModel> = mutableListOf()
    private var selectedCategory: String = ""
    private lateinit var productAdapter: ProductGridAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupViews()
        loadCategories()
        loadProducts()
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Setup grid layout for products
        binding.productsRecyclerView.layoutManager = GridLayoutManager(this, 2)
    }

    private fun loadCategories() {
        binding.progressBarCategory.visibility = View.VISIBLE

        viewModel.category.observe(this, Observer { categories ->
            binding.categoryRecyclerView.layoutManager =
                androidx.recyclerview.widget.LinearLayoutManager(
                    this,
                    androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL,
                    false
                )

            binding.categoryRecyclerView.adapter = CategoryAdapter(categories) { category ->
                selectedCategory = if (selectedCategory == category) "" else category
                filterProducts()
            }

            binding.progressBarCategory.visibility = View.GONE
        })

        viewModel.loadCategory()
    }

    private fun loadProducts() {
        binding.progressBarProducts.visibility = View.VISIBLE

        viewModel.popular.observe(this, Observer { products ->
            allProducts = products
            filteredProducts.clear()
            filteredProducts.addAll(products)

            updateProductsView()
            binding.progressBarProducts.visibility = View.GONE
        })

        viewModel.loadPopular()
    }

    private fun filterProducts() {
        filteredProducts.clear()

        if (selectedCategory.isEmpty()) {
            filteredProducts.addAll(allProducts)
        } else {
            for (product in allProducts) {
                if (product.title.contains(selectedCategory, ignoreCase = true)) {
                    filteredProducts.add(product)
                }
            }
        }

        updateProductsView()
    }

    private fun updateProductsView() {
        if (filteredProducts.isEmpty()) {
            binding.emptyTxt.visibility = View.VISIBLE
            binding.productsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyTxt.visibility = View.GONE
            binding.productsRecyclerView.visibility = View.VISIBLE

            productAdapter = ProductGridAdapter(filteredProducts, this)
            binding.productsRecyclerView.adapter = productAdapter
        }

        binding.productCountTxt.text = "${filteredProducts.size} Products"
    }
}