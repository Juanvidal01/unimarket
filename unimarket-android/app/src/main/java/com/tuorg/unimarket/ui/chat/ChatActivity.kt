package com.tuorg.unimarket.ui.chat

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.tuorg.unimarket.R
import com.tuorg.unimarket.models.CreateChatRequest
import com.tuorg.unimarket.models.Message
import com.tuorg.unimarket.models.SendMessageRequest
import com.tuorg.unimarket.network.ApiClient
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

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
        setContentView(R.layout.activity_chat)

        // Obtener datos del Intent
        sellerId = intent.getStringExtra("seller_id")
        sellerName = intent.getStringExtra("seller_name") ?: "Vendedor"
        productId = intent.getStringExtra("product_id")
        currentUserId = TokenStore.userId // Necesitas guardarlo en TokenStore al hacer login

        if (sellerId == null) {
            toast("Error: Vendedor no encontrado")
            finish()
            return
        }

        initViews()
        setupRecyclerView()
        setupListeners()

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
                progressBar.visibility = View.VISIBLE

                val request = CreateChatRequest(
                    otherUserId = sellerId!!,
                    productId = productId
                )

                val response = ApiClient.apiService.createChat(request)

                if (response.isSuccessful) {
                    val chat = response.body()
                    chatId = chat?.id

                    // Cargar mensajes existentes
                    loadMessages()
                } else {
                    toast("Error al crear chat: ${response.message()}")
                }

                progressBar.visibility = View.GONE

            } catch (e: Exception) {
                progressBar.visibility = View.GONE
                toast("Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun loadMessages() {
        if (chatId == null) return

        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.getChatMessages(chatId!!)

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
                }

            } catch (e: Exception) {
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
                val response = ApiClient.apiService.sendMessage(chatId!!, request)

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
                val response = ApiClient.apiService.getChatMessages(chatId!!)

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