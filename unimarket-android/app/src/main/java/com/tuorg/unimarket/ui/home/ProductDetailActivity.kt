package com.tuorg.unimarket.ui.home

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.ApiClient
import com.tuorg.unimarket.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var service: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)  // 👈 asegura nombre exacto

        val img = findViewById<ImageView>(R.id.imgHero)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvDesc  = findViewById<TextView>(R.id.tvDesc)
        val btnChat = findViewById<TextView>(R.id.btnChat)

        service = ApiClient.retrofit.create(ApiService::class.java)

        val id = intent.getStringExtra("product_id")
        if (id.isNullOrEmpty()) { finish(); return }

        service.getProductById(id).enqueue(object : Callback<Product> {
            override fun onResponse(call: Call<Product>, res: Response<Product>) {
                if (!res.isSuccessful) { toast("Error ${res.code()}"); return }
                val p = res.body() ?: return
                tvTitle.text = p.title
                tvPrice.text = "$${"%,.0f".format(p.price)}"
                tvDesc.text = p.description ?: ""
                p.images.firstOrNull()?.let { Glide.with(this@ProductDetailActivity).load(it).into(img) }
            }
            override fun onFailure(call: Call<Product>, t: Throwable) { toast("Fallo: ${t.message}") }
        })

        btnChat.setOnClickListener {
            toast("Chat con vendedor (próximo paso)")
        }
    }

    private fun toast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
