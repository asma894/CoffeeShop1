package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.cardview.widget.CardView
import com.example.coffeeshop.R
import com.example.coffeeshop.helper.ManagmentCart
import com.example.coffeeshop.model.OrderModel
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class CheckoutActivity : BaseActivity() {

    private lateinit var managementCart: ManagmentCart
    private lateinit var auth: FirebaseAuth
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://asma-348c0-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    private var selectedPaymentMethod = "Credit Card"
    private var totalAmount = 0.0

    private lateinit var subtotalTxt: TextView
    private lateinit var taxTxt: TextView
    private lateinit var deliveryTxt: TextView
    private lateinit var totalAmountTxt: TextView

    private lateinit var creditCardLayout: LinearLayout
    private lateinit var paypalLayout: LinearLayout
    private lateinit var cashLayout: LinearLayout

    private lateinit var creditCardRadio: RadioButton
    private lateinit var paypalRadio: RadioButton
    private lateinit var cashRadio: RadioButton

    private lateinit var cardNumberEdt: TextInputEditText
    private lateinit var cardHolderEdt: TextInputEditText
    private lateinit var expiryDateEdt: TextInputEditText
    private lateinit var cvvEdt: TextInputEditText

    private lateinit var placeOrderBtn: Button
    private lateinit var backBtn: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        auth = FirebaseAuth.getInstance()
        managementCart = ManagmentCart(this)

        initViews()
        calculateTotal()
        setupPaymentMethods()
        setupButtons()
    }

    private fun initViews() {
        subtotalTxt = findViewById(R.id.subtotalTxt)
        taxTxt = findViewById(R.id.taxTxt)
        deliveryTxt = findViewById(R.id.deliveryTxt)
        totalAmountTxt = findViewById(R.id.totalAmountTxt)

        creditCardLayout = findViewById(R.id.creditCardLayout)
        paypalLayout = findViewById(R.id.paypalLayout)
        cashLayout = findViewById(R.id.cashLayout)

        creditCardRadio = findViewById(R.id.creditCardRadio)
        paypalRadio = findViewById(R.id.paypalRadio)
        cashRadio = findViewById(R.id.cashRadio)

        cardNumberEdt = findViewById(R.id.cardNumberEdt)
        cardHolderEdt = findViewById(R.id.cardHolderEdt)
        expiryDateEdt = findViewById(R.id.expiryDateEdt)
        cvvEdt = findViewById(R.id.cvvEdt)

        placeOrderBtn = findViewById(R.id.placeOrderBtn)
        backBtn = findViewById(R.id.backBtn)
    }

    private fun calculateTotal() {
        val percentTax = 0.02
        val delivery = 15.0
        val subtotal = managementCart.getTotalFee()
        val tax = Math.round((subtotal * percentTax) * 100) / 100.0
        totalAmount = Math.round((subtotal + tax + delivery) * 100) / 100.0

        subtotalTxt.text = "$$subtotal"
        taxTxt.text = "$$tax"
        deliveryTxt.text = "$$delivery"
        totalAmountTxt.text = "$$totalAmount"
    }

    private fun setupPaymentMethods() {
        selectPaymentMethod("Credit Card")

        creditCardLayout.setOnClickListener {
            selectPaymentMethod("Credit Card")
        }

        paypalLayout.setOnClickListener {
            selectPaymentMethod("PayPal")
        }

        cashLayout.setOnClickListener {
            selectPaymentMethod("Cash on Delivery")
        }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method

        creditCardRadio.isChecked = false
        paypalRadio.isChecked = false
        cashRadio.isChecked = false

        when (method) {
            "Credit Card" -> creditCardRadio.isChecked = true
            "PayPal" -> paypalRadio.isChecked = true
            "Cash on Delivery" -> cashRadio.isChecked = true
        }
    }

    private fun setupButtons() {
        backBtn.setOnClickListener {
            finish()
        }

        placeOrderBtn.setOnClickListener {
            placeOrder()
        }
    }

    private fun placeOrder() {
        val currentUser = auth.currentUser

        if (currentUser == null) {
            Toast.makeText(this, "Please login to place an order", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            return
        }

        if (selectedPaymentMethod == "Credit Card") {
            val cardNumber = cardNumberEdt.text.toString().trim()
            val cardHolder = cardHolderEdt.text.toString().trim()
            val expiryDate = expiryDateEdt.text.toString().trim()
            val cvv = cvvEdt.text.toString().trim()

            if (cardNumber.isEmpty() || cardHolder.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Please fill in all card details", Toast.LENGTH_SHORT).show()
                return
            }

            if (cardNumber.length < 16) {
                Toast.makeText(this, "Invalid card number", Toast.LENGTH_SHORT).show()
                return
            }

            if (cvv.length < 3) {
                Toast.makeText(this, "Invalid CVV", Toast.LENGTH_SHORT).show()
                return
            }
        }

        placeOrderBtn.isEnabled = false
        placeOrderBtn.text = "Processing..."

        val orderId = firebaseDatabase.reference.child("Orders").push().key ?: return
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())

        val order = OrderModel(
            orderId = orderId,
            userId = currentUser.uid,
            userEmail = currentUser.email ?: "",
            items = managementCart.getListCart(),
            totalAmount = totalAmount,
            paymentMethod = selectedPaymentMethod,
            orderDate = currentDate,
            status = "Pending"
        )

        firebaseDatabase.reference.child("Orders")
            .child(orderId)
            .setValue(order)
            .addOnSuccessListener {
                managementCart.clearCart()

                Toast.makeText(this, "Order placed successfully!", Toast.LENGTH_LONG).show()

                val intent = Intent(this, OrderSuccessActivity::class.java)
                intent.putExtra("orderId", orderId)
                intent.putExtra("totalAmount", totalAmount)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                placeOrderBtn.isEnabled = true
                placeOrderBtn.text = "Place Order"
                Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }
}