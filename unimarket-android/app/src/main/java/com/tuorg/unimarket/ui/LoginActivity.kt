package com.tuorg.unimarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.*
import com.tuorg.unimarket.ui.home.HomeActivity   // 👈 IMPORTA AQUÍ

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {
    private val auth by lazy { ApiClient.retrofit.create(AuthService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)
        val linkGoRegister = findViewById<TextView>(R.id.linkGoRegister)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty()) {
                toast("Completa email y contraseña"); return@setOnClickListener
            }

            auth.login(LoginRequest(email, pass)).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, res: Response<AuthResponse>) {
                    if (res.isSuccessful) {
                        val token = res.body()?.token
                        TokenStore.jwt = token
                        // (opcional) persistir token
                        getSharedPreferences("auth", MODE_PRIVATE).edit().putString("jwt", token).apply()
                        toast("Bienvenido ${res.body()?.user?.name}")
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    } else {
                        toast("Error ${res.code()}")
                    }
                }
                override fun onFailure(call: Call<AuthResponse>, t: Throwable) {
                    toast("Fallo: ${t.message}")
                }
            })
        }

        linkGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
