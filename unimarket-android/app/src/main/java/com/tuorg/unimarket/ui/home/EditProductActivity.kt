package com.tuorg.unimarket.ui.home

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EditProductActivity : AppCompatActivity() {

    private lateinit var service: ApiService
    private lateinit var btnBack: ImageView
    private lateinit var btnDelete: ImageView
    private lateinit var imgPreview: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var spinnerStatus: Spinner
    private lateinit var btnSave: Button
    private lateinit var progressBar: ProgressBar

    private var productId: String? = null
    private var currentProduct: Product? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_product)

        service = ApiClient.retrofit.create(ApiService::class.java)

        productId = intent.getStringExtra("product_id")

        if (productId == null) {
            toast("Error: Producto no encontrado")
            finish()
            return
        }

        initViews()
        setupListeners()
        loadProduct()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnDelete = findViewById(R.id.btnDelete)
        imgPreview = findViewById(R.id.imgPreview)
        etTitle = findViewById(R.id.etTitle)
        etPrice = findViewById(R.id.etPrice)
        etDescription = findViewById(R.id.etDescription)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)

        // Configurar spinner de estado
        val statusOptions = arrayOf("publicado", "pausado", "vendido")
        spinnerStatus.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusOptions
        )
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }

        btnSave.setOnClickListener {
            saveChanges()
        }
    }

    private fun loadProduct() {
        progressBar.visibility = View.VISIBLE

        service.getProductById(productId!!).enqueue(object : Callback<ProductDetailResponse> {
            override fun onResponse(
                call: Call<ProductDetailResponse>,
                response: Response<ProductDetailResponse>
            ) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    currentProduct = response.body()?.product
                    fillForm()
                } else {
                    toast("Error al cargar producto")
                    finish()
                }
            }

            override fun onFailure(call: Call<ProductDetailResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                toast("Error: ${t.message}")
                finish()
            }
        })
    }

    private fun fillForm() {
        val product = currentProduct ?: return

        etTitle.setText(product.title)
        etPrice.setText(product.price.toString())
        etDescription.setText(product.description)

        // Seleccionar estado en spinner
        val statusPosition = when (product.status) {
            "publicado" -> 0
            "pausado" -> 1
            "vendido" -> 2
            else -> 0
        }
        spinnerStatus.setSelection(statusPosition)

        // Cargar imagen
        if (product.images.isNotEmpty()) {
            Glide.with(this)
                .load(product.images[0].url)
                .centerCrop()
                .into(imgPreview)
        }
    }

    private fun saveChanges() {
        val title = etTitle.text.toString().trim()
        val priceText = etPrice.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val status = spinnerStatus.selectedItem.toString()

        if (title.isEmpty() || priceText.isEmpty() || description.isEmpty()) {
            toast("Completa todos los campos")
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            toast("Precio inválido")
            return
        }

        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnSave.isEnabled = false

                val request = UpdateProductRequest(
                    title = title,
                    description = description,
                    price = price,
                    status = status
                )

                val response = service.updateProduct(productId!!, request)

                progressBar.visibility = View.GONE
                btnSave.isEnabled = true

                if (response.isSuccessful) {
                    toast("Producto actualizado ✅")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    toast("Error: ${response.message()}")
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnSave.isEnabled = true
                toast("Error: ${e.message}")
            }
        }
    }

    private fun confirmDelete() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar producto")
            .setMessage("¿Estás seguro de que quieres eliminar este producto? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                deleteProduct()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteProduct() {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE

                val response = service.deleteProduct(productId!!)

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    toast("Producto eliminado ✅")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    toast("Error al eliminar: ${response.message()}")
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                toast("Error: ${e.message}")
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}