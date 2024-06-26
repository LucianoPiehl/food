package com.example.food.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.food.data.UserRepository
import com.example.food.databinding.ActivityLoginBinding
import com.example.food.util.CustomTextWatcher
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar UserRepository con el contexto
        userRepository = UserRepository.getInstance(this)

        binding.ingresarBtn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.contrasenia.text.toString()

            // Usar corrutinas para la operación de login
            lifecycleScope.launch {
                val user = userRepository.login(email, password)

                if (user != null) {
                    val intent = Intent(this@LoginActivity, RecipesActivity::class.java)
                    intent.putExtra("USER_EMAIL", email)
                    startActivity(intent)
                } else {
                    binding.mensaje.setText("Las credenciales ingresadas no son validas")
                }
            }
        }

        binding.goToRegistro.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val textWatcher = CustomTextWatcher(binding.email, binding.contrasenia, binding.ingresarBtn) { isValid ->

        }
        binding.email.addTextChangedListener(textWatcher)
        binding.contrasenia.addTextChangedListener(textWatcher)
    }
}
