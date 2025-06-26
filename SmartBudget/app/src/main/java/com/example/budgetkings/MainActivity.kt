package com.example.SmartBudget

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etConfirmPassword: TextInputEditText
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Input fields
        etUsername = findViewById(R.id.usernameEditText)
        etEmail = findViewById(R.id.emailEditText)
        etPassword = findViewById(R.id.passwordEditText)
        etConfirmPassword = findViewById(R.id.confirmPasswordEditText)

        val btnRegister = findViewById<Button>(R.id.registerButton)
        val btnLogin = findViewById<Button>(R.id.btnLogin) // ✅ Corrected ID

        btnRegister.setOnClickListener {
            if (validateRegistration()) {
                registerUser()
            }
        }

        btnLogin.setOnClickListener {
            // ✅ Navigates to Login screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }

    private fun validateRegistration(): Boolean {
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        return when {
            username.isEmpty() -> {
                showError("Username cannot be empty")
                false
            }
            email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showError("Please enter a valid email")
                false
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
                false
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
                false
            }
            else -> true
        }
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString().trim()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser ?: return@addOnCompleteListener

                    // Send verification email
                    user.sendEmailVerification()

                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email
                    )

                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .document(user.uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            // ✅ Log in user and go to dashboard
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registered & logged in", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, Dashboard::class.java))
                                    finish()
                                }
                                .addOnFailureListener {
                                    Toast.makeText(this, "Login failed: ${it.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to save user info", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
