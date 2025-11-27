package com.example.coffeeshop.activity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.CompoundButton
import com.example.coffeeshop.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth

class SettingsActivity : BaseActivity() {

    private val binding: ActivitySettingsBinding by lazy {
        ActivitySettingsBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        sharedPreferences = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        // Load saved preferences
        binding.notificationsSwitch.isChecked = sharedPreferences.getBoolean("notifications", true)
        binding.darkModeSwitch.isChecked = sharedPreferences.getBoolean("darkMode", false)
        binding.soundSwitch.isChecked = sharedPreferences.getBoolean("sound", true)
        binding.autoPlaySwitch.isChecked = sharedPreferences.getBoolean("autoPlay", false)

        // Update email display if logged in
        if (auth.currentUser != null) {
            binding.accountEmailTxt.text = auth.currentUser?.email ?: "Not logged in"
            binding.accountSection.alpha = 1f
        } else {
            binding.accountEmailTxt.text = "Not logged in"
            binding.accountSection.alpha = 0.5f
        }
    }

    private fun setupListeners() {
        binding.backBtn.setOnClickListener {
            finish()
        }

        // Notifications toggle
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("notifications", isChecked)
        }

        // Dark mode toggle
        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("darkMode", isChecked)
            // You can implement actual dark mode theme switching here
        }

        // Sound toggle
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("sound", isChecked)
        }

        // Auto-play toggle
        binding.autoPlaySwitch.setOnCheckedChangeListener { _, isChecked ->
            saveSetting("autoPlay", isChecked)
        }

        // Account section
        binding.accountSection.setOnClickListener {
            if (auth.currentUser != null) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }

        // Language selection
        binding.languageSection.setOnClickListener {
            // Show language selection dialog
            showLanguageDialog()
        }

        // Privacy policy
        binding.privacySection.setOnClickListener {
            // Open privacy policy (you can add a webview or new activity)
        }

        // Terms of service
        binding.termsSection.setOnClickListener {
            // Open terms of service
        }

        // Help & Support
        binding.helpSection.setOnClickListener {
            // Open help section
        }

        // About
        binding.aboutSection.setOnClickListener {
            showAboutDialog()
        }

        // Logout button (only visible if logged in)
        if (auth.currentUser != null) {
            binding.logoutBtn.setOnClickListener {
                auth.signOut()
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        } else {
            binding.logoutBtn.alpha = 0.5f
            binding.logoutBtn.isEnabled = false
        }
    }

    private fun saveSetting(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "Arabic", "French", "Spanish")
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Select Language")
        builder.setItems(languages) { dialog, which ->
            val selectedLanguage = languages[which]
            binding.languageValueTxt.text = selectedLanguage
            sharedPreferences.edit().putString("language", selectedLanguage).apply()
        }
        builder.show()
    }

    private fun showAboutDialog() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("About Coffee Shop")
        builder.setMessage("Version 1.0\n\nA modern coffee ordering app with authentication and personalized experience.\n\n© 2024 Coffee Shop")
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }
}