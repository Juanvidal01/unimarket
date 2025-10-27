package com.tuorg.unimarket.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.ApiClient
import com.tuorg.unimarket.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeActivity : AppCompatActivity() {

    private lateinit var service: ApiService
    private lateinit var adapter: ProductAdapter

    private lateinit var recycler: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var chipAll: TextView
    private lateinit var chipFavs: TextView
    private lateinit var chipPublish: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        service = ApiClient.retrofit.create(ApiService::class.java)

        recycler = findViewById(R.id.recyclerProducts)
        etSearch = findViewById(R.id.etSearch)
        chipAll = findViewById(R.id.chipAll)
        chipFavs = findViewById(R.id.chipFavs)
        chipPublish = findViewById(R.id.chipPublish)

        // Grid 2 columnas
        recycler.layoutManager = GridLayoutManager(this, 2)

        // Adapter con onFav y onItemClick (abre detalle)
        adapter = ProductAdapter(
            items = emptyList(),
            onFavClick = { p ->
                toast("Fav: ${p.title}") // Próximo paso: persistir favoritos
            },
            onItemClick = { p ->
                val i = Intent(this, ProductDetailActivity::class.java)
                i.putExtra("product_id", p._id)
                startActivity(i)
            }
        )
        recycler.adapter = adapter

        // Buscar con botón
        findViewById<ImageView>(R.id.btnSearch).setOnClickListener {
            loadProducts(etSearch.text.toString().trim().ifEmpty { null })
        }

        // Buscar con Enter en el teclado
        etSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                loadProducts(etSearch.text.toString().trim().ifEmpty { null })
                true
            } else false
        }

        // Chips
        chipAll.setOnClickListener { selectChip(Chip.ALL); loadProducts(null) }
        chipFavs.setOnClickListener {
            selectChip(Chip.FAVS)
            toast("Favoritos (próximo paso)")
            // Aquí podrías filtrar localmente si guardas favs en SharedPreferences
        }
        chipPublish.setOnClickListener {
            selectChip(Chip.PUBLISH)
            toast("Publicar (próximo paso)")
            // Aquí abriríamos la pantalla para crear producto
        }

        // Carga inicial
        selectChip(Chip.ALL)
        loadProducts(null)
    }

    private fun loadProducts(q: String?) {
        service.getProducts(q).enqueue(object : Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, res: Response<List<Product>>) {
                if (res.isSuccessful) {
                    adapter.submit(res.body().orEmpty())
                } else {
                    toast("Error ${res.code()}")
                }
            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                toast("Fallo: ${t.message}")
            }
        })
    }

    private enum class Chip { ALL, FAVS, PUBLISH }

    private fun selectChip(which: Chip) {
        fun TextView.select(sel: Boolean) {
            setBackgroundResource(if (sel) R.drawable.bg_chip_selected else R.drawable.bg_chip_normal)
            setPadding(24, 12, 24, 12)
        }
        chipAll.select(which == Chip.ALL)
        chipFavs.select(which == Chip.FAVS)
        chipPublish.select(which == Chip.PUBLISH)
    }

    private fun toast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
