package com.example.coffeeshop.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.coffeeshop.R
import com.example.coffeeshop.databinding.ActivityProfileBinding
import com.example.coffeeshop.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage

class ProfileActivity : BaseActivity() {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private val firebaseDatabase = FirebaseDatabase.getInstance(
        "https://asma-348c0-default-rtdb.europe-west1.firebasedatabase.app/"
    )
    private val storage = FirebaseStorage.getInstance()

    private var selectedImageUri: Uri? = null
    private var currentProfileImageUrl: String = ""
    private var isEditMode = false

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImage.setImageURI(it)
            uploadProfileImage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setupViews()
        loadUserData()
    }

    private fun setupViews() {
        binding.backBtn.setOnClickListener { finish() }

        binding.profileImageContainer.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.editProfileBtn.setOnClickListener {
            toggleEditMode()
        }

        binding.saveBtn.setOnClickListener {
            if (isEditMode) {
                updateUserData()
            }
        }

        binding.shareAppSection.setOnClickListener {
            shareApp()
        }

        binding.contactUsSection.setOnClickListener {
            contactUs()
        }

        binding.logoutBtn.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, IntroActivity::class.java))
            finish()
        }

        // Initially disable editing
        setEditingEnabled(false)
    }

    private fun toggleEditMode() {
        isEditMode = !isEditMode
        setEditingEnabled(isEditMode)

        if (isEditMode) {
            binding.editProfileBtn.text = "Cancel"
            binding.saveBtn.visibility = View.VISIBLE
        } else {
            binding.editProfileBtn.text = "Edit Profile"
            binding.saveBtn.visibility = View.GONE
            loadUserData() // Reload to discard changes
        }
    }

    private fun setEditingEnabled(enabled: Boolean) {
        binding.nameEdt.isEnabled = enabled
        binding.phoneEdt.isEnabled = enabled
        binding.addressEdt.isEnabled = enabled

        val alpha = if (enabled) 1.0f else 0.7f
        binding.nameEdt.alpha = alpha
        binding.phoneEdt.alpha = alpha
        binding.addressEdt.alpha = alpha
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
                            binding.emailTxt.text = user.email
                            binding.phoneEdt.setText(user.phone)
                            binding.addressEdt.setText(user.address)

                            currentProfileImageUrl = user.profileImageUrl ?: ""
                            if (currentProfileImageUrl.isNotEmpty()) {
                                Glide.with(this@ProfileActivity)
                                    .load(currentProfileImageUrl)
                                    .placeholder(R.drawable.ic_profile_placeholder)
                                    .circleCrop()
                                    .into(binding.profileImage)
                            }
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

    private fun uploadProfileImage() {
        val currentUser = auth.currentUser ?: return
        val imageUri = selectedImageUri ?: return

        binding.progressBar.visibility = View.VISIBLE
        binding.profileImageContainer.isEnabled = false

        val storageRef = storage.reference
            .child("profile_images/${currentUser.uid}.jpg")

        storageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    currentProfileImageUrl = uri.toString()
                    saveProfileImageUrl(currentProfileImageUrl)
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.profileImageContainer.isEnabled = true
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveProfileImageUrl(imageUrl: String) {
        val currentUser = auth.currentUser ?: return

        firebaseDatabase.getReference("Users")
            .child(currentUser.uid)
            .child("profileImageUrl")
            .setValue(imageUrl)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                binding.profileImageContainer.isEnabled = true
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                binding.profileImageContainer.isEnabled = true
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
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
                address = address,
                profileImageUrl = currentProfileImageUrl
            )

            firebaseDatabase.getReference("Users")
                .child(currentUser.uid)
                .setValue(userModel)
                .addOnSuccessListener {
                    binding.saveBtn.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                    toggleEditMode() // Exit edit mode
                }
                .addOnFailureListener {
                    binding.saveBtn.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Check out Coffee Shop App!")
            putExtra(
                Intent.EXTRA_TEXT,
                "Hey! I've been using this amazing Coffee Shop app. " +
                        "Download it now and enjoy your favorite coffee! 🔥☕"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Share Coffee Shop App"))
    }

    private fun contactUs() {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("support@coffeeshop.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Coffee Shop App - Support Request")
            putExtra(
                Intent.EXTRA_TEXT,
                "Hello Coffee Shop Team,\n\n" +
                        "I would like to get in touch regarding:\n\n"
            )
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email via..."))
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
        }
    }
}