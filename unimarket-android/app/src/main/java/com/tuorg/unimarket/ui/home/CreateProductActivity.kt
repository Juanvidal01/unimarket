package com.tuorg.unimarket.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.tuorg.unimarket.R
import com.tuorg.unimarket.network.ApiClient
import com.tuorg.unimarket.network.ApiService
import com.tuorg.unimarket.network.CreateProductRequest
import com.tuorg.unimarket.network.CreateProductResponse
import com.tuorg.unimarket.network.UploadImagesResponse
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

class CreateProductActivity : AppCompatActivity() {

    private lateinit var service: ApiService
    private lateinit var btnBack: ImageView
    private lateinit var imgAddPhoto: LinearLayout
    private lateinit var imgPreview: ImageView
    private lateinit var etTitle: EditText
    private lateinit var etPrice: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnPublish: Button
    private lateinit var progressBar: ProgressBar

    private val selectedImages = mutableListOf<Uri>()

    // Launcher para seleccionar múltiples imágenes
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            selectedImages.clear()
            selectedImages.addAll(uris.take(5)) // Máximo 5 imágenes
            updateImagePreview()
            toast("${selectedImages.size} imagen(es) seleccionada(s)")
        }
    }

    // Launcher para permisos
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openImagePicker()
        } else {
            toast("Permiso denegado")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_product)

        service = ApiClient.retrofit.create(ApiService::class.java)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        imgAddPhoto = findViewById(R.id.imgAddPhoto)
        imgPreview = findViewById(R.id.imgPreview)
        etTitle = findViewById(R.id.etTitle)
        etPrice = findViewById(R.id.etPrice)
        etDescription = findViewById(R.id.etDescription)
        btnPublish = findViewById(R.id.btnPublish)
        progressBar = findViewById(R.id.progressBar)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        imgAddPhoto.setOnClickListener {
            checkPermissionAndOpenPicker()
        }

        btnPublish.setOnClickListener {
            validateAndPublish()
        }
    }

    private fun updateImagePreview() {
        if (selectedImages.isNotEmpty()) {
            // Mostrar la primera imagen como preview
            Glide.with(this)
                .load(selectedImages[0])
                .centerCrop()
                .into(imgPreview)

            imgPreview.visibility = View.VISIBLE
        } else {
            imgPreview.visibility = View.GONE
        }
    }

    private fun checkPermissionAndOpenPicker() {
        when {
            // Android 13+ (API 33+)
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_MEDIA_IMAGES) -> {
                        // Mostrar explicación al usuario
                        toast("Necesitamos acceso a tus fotos para publicar productos")
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                    }
                }
            }
            // Android 10-12 (API 29-32)
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q -> {
                // En Android 10+, no necesitamos READ_EXTERNAL_STORAGE para leer imágenes
                openImagePicker()
            }
            // Android 9 y anteriores
            else -> {
                when {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED -> {
                        openImagePicker()
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                        toast("Necesitamos acceso a tus fotos para publicar productos")
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                    else -> {
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        imagePickerLauncher.launch("image/*")
    }

    private fun validateAndPublish() {
        val title = etTitle.text.toString().trim()
        val description = etDescription.text.toString().trim()
        val priceText = etPrice.text.toString().trim()

        // Validaciones
        if (title.isEmpty()) {
            toast("Por favor agrega un título")
            etTitle.requestFocus()
            return
        }
        if (priceText.isEmpty()) {
            toast("Por favor agrega un precio")
            etPrice.requestFocus()
            return
        }
        if (description.isEmpty()) {
            toast("Por favor agrega una descripción")
            etDescription.requestFocus()
            return
        }
        if (selectedImages.isEmpty()) {
            toast("Por favor selecciona al menos una foto")
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            toast("Precio inválido")
            etPrice.requestFocus()
            return
        }

        publishProduct(title, description, price)
    }

    private fun publishProduct(
        title: String,
        description: String,
        price: Double
    ) {
        lifecycleScope.launch {
            try {
                progressBar.visibility = View.VISIBLE
                btnPublish.isEnabled = false
                btnPublish.text = "Publicando..."

                // ========== PASO 1: Crear producto (sin imágenes) ==========
                val productRequest = CreateProductRequest(
                    title = title,
                    description = description,
                    category = "Otros", // Por defecto
                    price = price,
                    condition = "usado", // Por defecto
                    location = null,
                    keywords = emptyList(),
                    images = emptyList()
                )

                val createResponse: Response<CreateProductResponse> =
                    service.createProductSuspend(productRequest)

                if (!createResponse.isSuccessful) {
                    val errorBody = createResponse.errorBody()?.string()
                    throw Exception("Error al crear producto: ${errorBody ?: createResponse.message()}")
                }

                val createdProduct = createResponse.body()?.product
                    ?: throw Exception("No se recibió el producto creado")

                val productId = createdProduct._id

                // ========== PASO 2: Subir imágenes ==========
                val imageParts = mutableListOf<MultipartBody.Part>()
                for ((index, uri) in selectedImages.withIndex()) {
                    val file = uriToFile(uri, index)
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    val part = MultipartBody.Part.createFormData("images", file.name, requestFile)
                    imageParts.add(part)
                }

                val uploadResponse: Response<UploadImagesResponse> =
                    service.uploadProductImagesSuspend(productId, imageParts)

                progressBar.visibility = View.GONE
                btnPublish.isEnabled = true
                btnPublish.text = "PUBLICAR"

                if (uploadResponse.isSuccessful) {
                    toast("¡Producto publicado exitosamente!")
                    setResult(RESULT_OK)
                    finish()
                } else {
                    val errorBody = uploadResponse.errorBody()?.string()
                    toast("Producto creado pero error al subir imágenes: ${errorBody ?: uploadResponse.message()}")
                }

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                btnPublish.isEnabled = true
                btnPublish.text = "PUBLICAR"

                toast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun uriToFile(uri: Uri, index: Int): File {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(cacheDir, "product_image_${System.currentTimeMillis()}_$index.jpg")
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    private fun toast(s: String) =
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}