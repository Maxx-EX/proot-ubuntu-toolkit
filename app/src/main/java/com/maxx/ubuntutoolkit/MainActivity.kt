package com.maxx.ubuntutoolkit

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private var launcher: ProotLauncher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            status.text = "正在启动 PRoot..."
            launcher = ProotLauncher(this)
            status.text = if (launcher!!.startUbuntu()) "Ubuntu 已启动" else "启动失败"
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            launcher?.stop(); status.text = "已停止"
        }
    }

    override fun onDestroy() { launcher?.stop(); super.onDestroy() }
}
