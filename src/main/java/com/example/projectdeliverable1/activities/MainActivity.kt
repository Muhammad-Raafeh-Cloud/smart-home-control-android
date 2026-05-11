package com.example.projectdeliverable1.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.projectdeliverable1.R
import com.example.projectdeliverable1.data.AuthRepository
import com.example.projectdeliverable1.data.FirestoreHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var tvStatus: TextView

    private val googleLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            lifecycleScope.launch {
                val authResult = AuthRepository.signInWithGoogle(account)
                authResult.onSuccess { user ->
                    FirestoreHelper.saveUserProfile(
                        user.uid,
                        user.email ?: "google-user@firebase.local",
                        user.displayName ?: "Google User"
                    )
                    openDashboard(user.displayName ?: user.email ?: "Google User")
                }.onFailure { error ->
                    tvStatus.text = error.message ?: "Google sign-in failed"
                }
            }
        } catch (exception: Exception) {
            tvStatus.text = exception.message ?: "Google sign-in cancelled"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AuthRepository.isLoggedIn()) {
            val user = AuthRepository.getCurrentUser()
            openDashboard(user?.displayName ?: user?.email ?: "Firebase User")
            finish()
            return
        }

        setContentView(R.layout.activity_main)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        tvStatus = findViewById(R.id.tvAuthStatus)

        findViewById<Button>(R.id.btnEmailLogin).setOnClickListener { signInWithEmail() }
        findViewById<Button>(R.id.btnEmailRegister).setOnClickListener { registerWithEmail() }
        findViewById<Button>(R.id.btnGoogleLogin).setOnClickListener { signInWithGoogle() }
    }

    private fun signInWithEmail() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        if (!validateEmailPassword(email, password)) return

        tvStatus.text = "Signing in..."
        lifecycleScope.launch {
            AuthRepository.signInWithEmail(email, password)
                .onSuccess { user ->
                    FirestoreHelper.saveUserProfile(user.uid, user.email ?: email, user.displayName ?: email)
                    openDashboard(user.email ?: email)
                }
                .onFailure { tvStatus.text = it.message ?: "Sign-in failed" }
        }
    }

    private fun registerWithEmail() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        if (!validateEmailPassword(email, password)) return

        tvStatus.text = "Creating account..."
        lifecycleScope.launch {
            AuthRepository.registerWithEmail(email, password)
                .onSuccess { user ->
                    FirestoreHelper.saveUserProfile(user.uid, user.email ?: email, user.displayName ?: email)
                    openDashboard(user.email ?: email)
                }
                .onFailure { tvStatus.text = it.message ?: "Registration failed" }
        }
    }

    private fun signInWithGoogle() {
        val tokenName = "default_web_client_id"
        val tokenId = resources.getIdentifier(tokenName, "string", packageName)
        if (tokenId == 0) {
            Toast.makeText(
                this,
                "Google Sign-In needs a Web OAuth client in Firebase. Email/password is ready.",
                Toast.LENGTH_LONG
            ).show()
            tvStatus.text = "Add a Web client in Firebase Authentication for Google Sign-In."
            return
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(tokenId))
            .requestEmail()
            .build()
        googleLauncher.launch(GoogleSignIn.getClient(this, gso).signInIntent)
    }

    private fun validateEmailPassword(email: String, password: String): Boolean {
        return when {
            email.isBlank() || password.isBlank() -> {
                tvStatus.text = "Enter email and password."
                false
            }
            password.length < 6 -> {
                tvStatus.text = "Password must be at least 6 characters."
                false
            }
            else -> true
        }
    }

    private fun openDashboard(userName: String) {
        startActivity(Intent(this, DashboardActivity::class.java).apply {
            putExtra(DashboardActivity.EXTRA_USER_NAME, userName)
        })
    }
}
