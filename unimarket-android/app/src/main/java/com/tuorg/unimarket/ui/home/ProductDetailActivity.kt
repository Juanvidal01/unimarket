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
import com.tuorg.unimarket.network.ProductDetailResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.NumberFormat
import java.util.*

class ProductDetailActivity : AppCompatActivity() {

    private lateinit var service: ApiService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val img = findViewById<ImageView>(R.id.imgHero)
        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val tvDesc = findViewById<TextView>(R.id.tvDesc)
        val btnChat = findViewById<TextView>(R.id.btnChat)

        service = ApiClient.retrofit.create(ApiService::class.java)



        val id = intent.getStringExtra("product_id")
        if (id.isNullOrEmpty()) {
            toast("ID de producto inválido")
            finish()
            return
        }

        // Llamada al API con la respuesta correcta: { product: {...} }
        service.getProductById(id).enqueue(object : Callback<ProductDetailResponse> {
            override fun onResponse(
                call: Call<ProductDetailResponse>,
                res: Response<ProductDetailResponse>
            ) {
                if (!res.isSuccessful) {
                    toast("Error ${res.code()}: ${res.message()}")
                    return
                }

                val productResponse = res.body()
                if (productResponse == null) {
                    toast("No se encontró el producto")
                    return
                }

                val p = productResponse.product

                // Título
                tvTitle.text = p.title

                // Precio formateado
                val priceFormatted = NumberFormat.getCurrencyInstance(Locale("es", "CO"))
                    .format(p.price)
                tvPrice.text = priceFormatted

                // Descripción
                tvDesc.text = p.description

                // Imagen principal (primera imagen)
                if (p.images.isNotEmpty()) {
                    Glide.with(this@ProductDetailActivity)
                        .load(p.images[0].url)
                        .centerCrop()
                        .placeholder(R.color.input_bg)
                        .error(R.color.input_bg)
                        .into(img)
                }
            }

            override fun onFailure(call: Call<ProductDetailResponse>, t: Throwable) {
                toast("Error de conexión: ${t.message}")
                t.printStackTrace()
            }
        })

        btnChat.setOnClickListener {
            toast("Chat con vendedor (próxima feature)")
            // TODO: Abrir chat con el vendedor
        }
    }

    private fun toast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}