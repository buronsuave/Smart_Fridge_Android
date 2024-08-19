package com.example.smartfridge.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.smartfridge.R
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class LoginActivity : AppCompatActivity() {
    private lateinit var edtxtUsername : EditText
    private lateinit var edtxtPassword : EditText
    private lateinit var btnLogin : Button

    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edtxtUsername = findViewById(R.id.edtxt_username)
        edtxtPassword = findViewById(R.id.edtxt_password)
        btnLogin = findViewById(R.id.btn_login)

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        var username = edtxtUsername.text.toString()
        var password = edtxtPassword.text.toString()

        if (username.isEmpty()) {
            edtxtUsername.error = "Username is required"
            return
        }

        if (password.isEmpty()) {
            edtxtPassword.error = "Password is required"
            return
        }

        db.collection("users").get()
            .addOnSuccessListener { result ->
                var valid = false
                for (document in result) {
                    if (document.data["username"] == username && document.data["password"] == password) {
                        valid = true
                        break
                    }
                }
                if (valid) {
                    val intent = Intent(this, MainMenuActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Bad Credentials. Try again.", Toast.LENGTH_LONG).show()
                }
            }
    }
}