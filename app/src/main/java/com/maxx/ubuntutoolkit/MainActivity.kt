package com.maxx.ubuntutoolkit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private var launcher: ProotLauncher? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        findViewById<TextView>(R.id.archText).text = "架构: ${NativeLib.getArch()}"

        findViewById<ImageButton>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            status.text = "正在启动 PRoot..."
            launcher = ProotLauncher(this)
            status.text = if (launcher!!.startUbuntu()) "Ubuntu 运行中" else "启动失败，查看日志"
        }
        findViewById<Button>(R.id.btnStop).setOnClickListener {
            launcher?.stop(); status.text = getString(R.string.status_ready)
        }
    }

    override fun onDestroy() { launcher?.stop(); super.onDestroy() }
}
