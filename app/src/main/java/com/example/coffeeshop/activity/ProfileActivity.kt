package com.example.coffeeshop.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.example.coffeeshop.databinding.ActivityProfileBinding
import com.example.coffeeshop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : BaseActivity() {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://asma-348c0-default-rtdb.europe-west1.firebasedatabase.app/"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if user is logged in
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        loadUserData()

        binding.saveBtn.setOnClickListener {
            updateUserData()
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            binding.progressBar.visibility = View.VISIBLE

            firebaseDatabase.getReference("Users")
                .child(currentUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        binding.progressBar.visibility = View.GONE
                        val user = snapshot.getValue(UserModel::class.java)
                        if (user != null) {
                            binding.nameEdt.setText(user.name)
                            binding.emailEdt.setText(user.email)
                            binding.phoneEdt.setText(user.phone)
                            binding.addressEdt.setText(user.address)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@ProfileActivity,
                            "Failed to load user data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
    }

    private fun updateUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val name = binding.nameEdt.text.toString().trim()
            val phone = binding.phoneEdt.text.toString().trim()
            val address = binding.addressEdt.text.toString().trim()

            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Name and phone are required", Toast.LENGTH_SHORT).show()
                return
            }

            binding.saveBtn.isEnabled = false
            binding.progressBar.visibility = View.VISIBLE

            val userModel = UserModel(
                uid = currentUser.uid,
                name = name,
                email = currentUser.email ?: "",
                phone = phone,
                address = address
            )

            firebaseDatabase.getReference("Users")
                .child(currentUser.uid)
                .setValue(userModel)
                .addOnSuccessListener {
                    binding.saveBtn.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    binding.saveBtn.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }
}