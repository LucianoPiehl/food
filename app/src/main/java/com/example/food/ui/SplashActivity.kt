package com.example.food.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.food.R
import com.example.food.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        binding.statusText.text = getString(
            if (currentUser != null) {
                R.string.splash_status_session
            } else {
                R.string.splash_status_loading
            }
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val nextIntent = if (currentUser != null) {
                Intent(this, RecipesActivity::class.java).apply {
                    putExtra("USER_EMAIL", currentUser.email ?: "")
                }
            } else {
                Intent(this, LoginActivity::class.java)
            }
            startActivity(nextIntent)
            finish()
        }, 1400)
    }
}
