package com.tuorg.unimarket.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuorg.unimarket.R
import com.tuorg.unimarket.models.*
import com.tuorg.unimarket.network.ApiClient
import com.tuorg.unimarket.network.ApiService
import com.tuorg.unimarket.network.TokenStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var service: ApiService
    private lateinit var btnBack: ImageView
    private lateinit var tvSellerName: TextView
    private lateinit var recyclerMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var progressBar: ProgressBar

    private lateinit var adapter: MessageAdapter
    private val messages = mutableListOf<Message>()

    private var chatId: String? = null
    private var sellerId: String? = null
    private var sellerName: String? = null
    private var productId: String? = null
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        android.util.Log.d("DEBUG_CHAT", "ChatActivity onCreate iniciado")

        setContentView(R.layout.activity_chat)

        service = ApiClient.retrofit.create(ApiService::class.java)

        // Obtener datos del Intent
        sellerId = intent.getStringExtra("seller_id")
        sellerName = intent.getStringExtra("seller_name") ?: "Vendedor"
        productId = intent.getStringExtra("product_id")
        currentUserId = TokenStore.userId

        android.util.Log.d("DEBUG_CHAT", "Datos recibidos:")
        android.util.Log.d("DEBUG_CHAT", "sellerId: $sellerId")
        android.util.Log.d("DEBUG_CHAT", "sellerName: $sellerName")
        android.util.Log.d("DEBUG_CHAT", "productId: $productId")
        android.util.Log.d("DEBUG_CHAT", "currentUserId: $currentUserId")

        if (sellerId == null) {
            android.util.Log.e("DEBUG_CHAT", "ERROR: sellerId es null")
            toast("Error: Vendedor no encontrado")
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupListeners()

        android.util.Log.d("DEBUG_CHAT", "Llamando createOrGetChat()")
        // Crear o obtener chat
        createOrGetChat()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvSellerName = findViewById(R.id.tvSellerName)
        recyclerMessages = findViewById(R.id.recyclerMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        progressBar = findViewById(R.id.progressBar)

        tvSellerName.text = sellerName
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(messages, currentUserId ?: "")
        recyclerMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true // Mostrar últimos mensajes abajo
        }
        recyclerMessages.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSend.setOnClickListener {
            sendMessage()
        }

        // Enviar con Enter
        etMessage.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }
    }

    private fun createOrGetChat() {
        lifecycleScope.launch {
            try {
                android.util.Log.d("DEBUG_CHAT", "createOrGetChat: iniciando coroutine")
                progressBar.visibility = View.VISIBLE

                val request = CreateChatRequest(
                    otherUserId = sellerId!!,
                    productId = productId
                )

                android.util.Log.d("DEBUG_CHAT", "Enviando request: $request")

                // Usar await() para suspend functions
                val response = service.createChat(request)

                android.util.Log.d("DEBUG_CHAT", "Response code: ${response.code()}")
                android.util.Log.d("DEBUG_CHAT", "Response isSuccessful: ${response.isSuccessful}")

                progressBar.visibility = View.GONE

                if (response.isSuccessful) {
                    val chat = response.body()
                    android.util.Log.d("DEBUG_CHAT", "Chat recibido: $chat")

                    if (chat != null) {
                        chatId = chat.id
                        android.util.Log.d("DEBUG_CHAT", "Chat ID asignado: $chatId")
                        toast("Chat iniciado")
                        // Cargar mensajes existentes
                        loadMessages()
                    } else {
                        android.util.Log.e("DEBUG_CHAT", "Chat es null en response")
                        toast("Error: No se recibió el chat")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    android.util.Log.e("DEBUG_CHAT", "Error response: $errorBody")
                    toast("Error al crear chat: ${errorBody ?: response.message()}")
                }

            } catch (e: Exception) {
                android.util.Log.e("DEBUG_CHAT", "Exception en createOrGetChat", e)
                progressBar.visibility = View.GONE
                toast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadMessages() {
        if (chatId == null) {
            toast("Error: Chat ID no disponible")
            return
        }

        lifecycleScope.launch {
            try {
                val response = service.getChatMessages(chatId!!)

                if (response.isSuccessful) {
                    val loadedMessages = response.body() ?: emptyList()
                    messages.clear()
                    messages.addAll(loadedMessages)
                    adapter.notifyDataSetChanged()

                    // Scroll al último mensaje
                    if (messages.isNotEmpty()) {
                        recyclerMessages.smoothScrollToPosition(messages.size - 1)
                    }

                    // Auto-refresh cada 3 segundos
                    startAutoRefresh()
                } else {
                    toast("Error al cargar mensajes: ${response.message()}")
                }

            } catch (e: Exception) {
                toast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun sendMessage() {
        val content = etMessage.text.toString().trim()

        if (content.isEmpty()) {
            toast("Escribe un mensaje")
            return
        }

        if (chatId == null) {
            toast("Chat no iniciado aún")
            return
        }

        lifecycleScope.launch {
            try {
                btnSend.isEnabled = false

                val request = SendMessageRequest(content)
                val response = service.sendMessage(chatId!!, request)

                if (response.isSuccessful) {
                    val newMessage = response.body()
                    if (newMessage != null) {
                        messages.add(newMessage)
                        adapter.notifyItemInserted(messages.size - 1)
                        recyclerMessages.smoothScrollToPosition(messages.size - 1)
                    }

                    // Limpiar input
                    etMessage.text.clear()
                } else {
                    toast("Error al enviar: ${response.message()}")
                }

                btnSend.isEnabled = true

            } catch (e: Exception) {
                btnSend.isEnabled = true
                toast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun startAutoRefresh() {
        lifecycleScope.launch {
            while (true) {
                delay(3000) // Cada 3 segundos
                refreshMessages()
            }
        }
    }

    private fun refreshMessages() {
        if (chatId == null) return

        lifecycleScope.launch {
            try {
                val response = service.getChatMessages(chatId!!)

                if (response.isSuccessful) {
                    val newMessages = response.body() ?: emptyList()

                    // Solo actualizar si hay nuevos mensajes
                    if (newMessages.size > messages.size) {
                        val oldSize = messages.size
                        messages.clear()
                        messages.addAll(newMessages)
                        adapter.notifyItemRangeInserted(oldSize, newMessages.size - oldSize)
                        recyclerMessages.smoothScrollToPosition(messages.size - 1)
                    }
                }

            } catch (e: Exception) {
                // Ignorar errores en refresh automático
            }
        }
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}