package com.maxx.ubuntutoolkit

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread

class ChatActivity : AppCompatActivity() {

    private data class Msg(val role: String, var content: String)

    private val history = mutableListOf<Msg>()
    private lateinit var adapter: ChatAdapter
    private lateinit var etInput: EditText
    private lateinit var btnSend: Button
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val tvModel = findViewById<TextView>(R.id.tvModel)
        tvModel.text = Settings.aiModel(this)

        val rv = findViewById<RecyclerView>(R.id.chatList)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(history)
        rv.adapter = adapter

        etInput = findViewById(R.id.etInput)
        btnSend = findViewById(R.id.btnSend)

        if (!Settings.aiConfigured(this)) {
            addMsg("assistant", "⚠️ 还没配置 API Key。请先在设置里填入 Base URL、API Key 和模型名称。")
        } else {
            addMsg("assistant", "你好！我是 AI 助手，有什么可以帮你的？")
        }

        btnSend.setOnClickListener {
            val text = etInput.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener
            etInput.setText("")
            sendMessage(text)
        }
    }

    private fun addMsg(role: String, content: String) {
        history.add(Msg(role, content))
        adapter.notifyItemInserted(history.size - 1)
        findViewById<RecyclerView>(R.id.chatList).scrollToPosition(history.size - 1)
    }

    private fun updateLastMsg(content: String) {
        if (history.isEmpty()) return
        history[history.size - 1].content = content
        adapter.notifyItemChanged(history.size - 1)
    }

    private fun sendMessage(userText: String) {
        addMsg("user", userText)
        addMsg("assistant", "")
        btnSend.isEnabled = false

        thread {
            try {
                val baseUrl = Settings.aiBaseUrl(this).trimEnd('/')
                val apiKey = Settings.aiApiKey(this)
                val model = Settings.aiModel(this)

                val messagesArr = JSONArray()
                for (m in history) {
                    if (m.content.isNotBlank()) {
                        messagesArr.put(JSONObject().put("role", m.role).put("content", m.content))
                    }
                }

                val body = JSONObject()
                    .put("model", model)
                    .put("messages", messagesArr)
                    .put("stream", true)

                val conn = (URL("$baseUrl/chat/completions").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("Authorization", "Bearer $apiKey")
                    doOutput = true
                    connectTimeout = 15000
                    readTimeout = 60000
                }
                conn.outputStream.use { it.write(body.toString().toByteArray()) }

                val code = conn.responseCode
                if (code != 200) {
                    val err = conn.errorStream?.bufferedReader()?.readText() ?: "HTTP $code"
                    mainHandler.post {
                        updateLastMsg("❌ 请求失败: $err")
                        btnSend.isEnabled = true
                    }
                    return@thread
                }

                val sb = StringBuilder()
                conn.inputStream.bufferedReader().forEachLine { line ->
                    if (!line.startsWith("data: ")) return@forEachLine
                    val data = line.removePrefix("data: ").trim()
                    if (data == "[DONE]") return@forEachLine
                    try {
                        val j = JSONObject(data)
                        val delta = j.getJSONArray("choices").getJSONObject(0)
                            .optJSONObject("delta")?.optString("content") ?: ""
                        if (delta.isNotEmpty()) {
                            sb.append(delta)
                            val current = sb.toString()
                            mainHandler.post { updateLastMsg(current) }
                        }
                    } catch (_: Exception) {}
                }

                mainHandler.post { btnSend.isEnabled = true }
            } catch (e: Exception) {
                mainHandler.post {
                    updateLastMsg("❌ ${e.message}")
                    btnSend.isEnabled = true
                }
            }
        }
    }

    private class ChatAdapter(val items: List<Msg>) : RecyclerView.Adapter<ChatAdapter.VH>() {
        class VH(v: View) : RecyclerView.ViewHolder(v) {
            val tv: TextView = v.findViewById(android.R.id.text1)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val v = LayoutInflater.from(parent.context)
                .inflate(android.R.layout.simple_list_item_1, parent, false)
            return VH(v)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            val m = items[position]
            val prefix = if (m.role == "user") "你: " else "AI: "
            holder.tv.text = prefix + m.content
            holder.tv.setPadding(24, 24, 24, 24)
            holder.tv.textSize = 15f
            holder.tv.setTextColor(0xFFFFFFFF.toInt())
        }

        override fun getItemCount() = items.size
    }
}
