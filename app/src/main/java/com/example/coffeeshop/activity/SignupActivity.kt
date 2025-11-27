package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.example.coffeeshop.databinding.ActivitySignupBinding
import com.example.coffeeshop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : BaseActivity() {

    private val binding: ActivitySignupBinding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://asma-348c0-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        binding.signupBtn.setOnClickListener {
            val name = binding.nameEdt.text.toString().trim()
            val email = binding.emailEdt.text.toString().trim()
            val phone = binding.phoneEdt.text.toString().trim()
            val password = binding.passwordEdt.text.toString().trim()
            val confirmPassword = binding.confirmPasswordEdt.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() ||
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            signupUser(name, email, phone, password)
        }

        binding.loginTxt.setOnClickListener {
            finish()
        }
    }

    private fun signupUser(name: String, email: String, phone: String, password: String) {
        binding.signupBtn.isEnabled = false

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val userModel = UserModel(
                        uid = user?.uid ?: "",
                        name = name,
                        email = email,
                        phone = phone,
                        address = ""
                    )

                    firebaseDatabase.getReference("Users")
                        .child(user?.uid ?: "")
                        .setValue(userModel)
                        .addOnSuccessListener {
                            binding.signupBtn.isEnabled = true
                            Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener {
                            binding.signupBtn.isEnabled = true
                            Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    binding.signupBtn.isEnabled = true
                    Toast.makeText(
                        this,
                        "Signup failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}