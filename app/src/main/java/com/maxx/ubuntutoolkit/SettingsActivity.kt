package com.maxx.ubuntutoolkit

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        val p = Settings.prefs(this)

        val etRootfs = findViewById<EditText>(R.id.etRootfs)
        val etLog = findViewById<EditText>(R.id.etLog)
        val swVerbose = findViewById<Switch>(R.id.swVerbose)

        etRootfs.setText(Settings.rootfs(this))
        etLog.setText(Settings.log(this))
        swVerbose.isChecked = Settings.verbose(this)

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            p.edit()
                .putString(Settings.KEY_ROOTFS, etRootfs.text.toString())
                .putString(Settings.KEY_LOG, etLog.text.toString())
                .putBoolean(Settings.KEY_VERBOSE, swVerbose.isChecked)
                .apply()
            Toast.makeText(this, "已保存", Toast.LENGTH_SHORT).show()
            finish()
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            p.edit().clear().apply()
            etRootfs.setText(Settings.rootfs(this))
            etLog.setText(Settings.log(this))
            swVerbose.isChecked = false
            Toast.makeText(this, "已恢复默认", Toast.LENGTH_SHORT).show()
        }
    }
}
