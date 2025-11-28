package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import com.example.coffeeshop.databinding.ActivityOrderSuccessBinding

class OrderSuccessActivity : BaseActivity() {

    private val binding: ActivityOrderSuccessBinding by lazy {
        ActivityOrderSuccessBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val orderId = intent.getStringExtra("orderId")
        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        binding.orderIdTxt.text = "Order ID: #$orderId"
        binding.totalAmountTxt.text = "$${"%.2f".format(totalAmount)}"

        binding.backToHomeBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}