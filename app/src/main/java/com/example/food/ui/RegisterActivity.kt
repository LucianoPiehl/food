package com.example.food.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.food.R
import com.example.food.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.GoogleAuthProvider

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                val accountTask = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                val account = accountTask.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (exception: ApiException) {
                showLoading(false)
                val message = when (exception.statusCode) {
                    12501 -> getString(R.string.login_error_cancelled)
                    10, 12500 -> getString(R.string.login_error_firebase_config)
                    else -> getString(R.string.register_error_generic)
                }
                showMessage(message)
            } catch (_: Exception) {
                showLoading(false)
                showMessage(getString(R.string.register_error_generic))
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        firebaseAuth = FirebaseAuth.getInstance()
        googleSignInClient = buildGoogleSignInClient()

        binding.googleSignInButton.setSize(SignInButton.SIZE_WIDE)
        binding.googleSignInButton.setOnClickListener {
            showLoading(true)
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

        binding.volverLogin.setOnClickListener {
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        if (firebaseAuth.currentUser != null) {
            navigateToRecipes(firebaseAuth.currentUser?.email)
        }
    }

    private fun buildGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(this, googleSignInOptions)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        val idToken = account?.idToken
        if (idToken.isNullOrBlank()) {
            showLoading(false)
            showMessage(getString(R.string.register_error_generic))
            return
        }

        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                showLoading(false)
                navigateToRecipes(firebaseAuth.currentUser?.email)
            }
            .addOnFailureListener { exception ->
                showLoading(false)
                showMessage(resolveFirebaseError(exception))
            }
    }

    private fun navigateToRecipes(email: String?) {
        val intent = Intent(this@RegisterActivity, RecipesActivity::class.java).apply {
            putExtra("USER_EMAIL", email ?: "")
        }
        startActivity(intent)
        finishAffinity()
    }

    private fun resolveFirebaseError(exception: Exception): String {
        return when (exception) {
            is FirebaseAuthException -> {
                if (exception.errorCode == "ERROR_OPERATION_NOT_ALLOWED") {
                    getString(R.string.register_error_provider_disabled)
                } else {
                    getString(R.string.register_error_generic)
                }
            }
            else -> getString(R.string.register_error_generic)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.googleSignInButton.isEnabled = !isLoading
        binding.volverLogin.isEnabled = !isLoading
        binding.registerProgress.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.loadingText.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading) {
            binding.mensaje.visibility = View.GONE
        }
    }

    private fun showMessage(message: String) {
        binding.mensaje.text = message
        binding.mensaje.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
