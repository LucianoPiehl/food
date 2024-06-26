package com.example.food.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.food.data.UserRepository
import com.example.food.databinding.ActivityRegisterBinding
import com.example.food.model.User
import com.example.food.util.CustomTextWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        userRepository = UserRepository.getInstance(this)

        val textWatcher = CustomTextWatcher(binding.email, binding.contrasenia, binding.registrarBtn) { isValid ->

        }
        binding.email.addTextChangedListener(textWatcher)
        binding.contrasenia.addTextChangedListener(textWatcher)


        binding.registrarBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.contrasenia.text.toString()

            val user = User(email, password, "", "")
            lifecycleScope.launch {
                val isRegistered = withContext(Dispatchers.IO){userRepository.register(user)}

                if (isRegistered) {
                    val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    // Show registration error
                }
            }
        }
    }
}