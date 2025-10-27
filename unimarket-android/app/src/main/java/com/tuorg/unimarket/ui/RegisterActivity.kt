package com.tuorg.unimarket.ui

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {
    private val auth by lazy { ApiClient.retrofit.create(AuthService::class.java) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegister = findViewById<TextView>(R.id.btnRegister)
        val linkGoLogin = findViewById<TextView>(R.id.linkGoLogin)

        btnRegister.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()
            if (name.isEmpty() || email.isEmpty() || pass.isEmpty()) {
                toast("Completa nombre, email y contraseña")
                return@setOnClickListener
            }

            auth.register(RegisterRequest(name, email, pass)).enqueue(object : Callback<AuthResponse> {
                override fun onResponse(call: Call<AuthResponse>, res: Response<AuthResponse>) {
                    if (res.isSuccessful) {
                        TokenStore.jwt = res.body()?.token
                        toast("Cuenta creada. ¡Bienvenido!")
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
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

        linkGoLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
