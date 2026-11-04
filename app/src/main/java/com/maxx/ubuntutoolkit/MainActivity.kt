package com.maxx.ubuntutoolkit

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private var proc: Process? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            status.text = "正在检查并解压 OBB..."
            Thread {
                val rootfs = File(filesDir, "ubuntu")
                val obbFile = File("/sdcard/Android/obb/$packageName/main.1.com.maxx.ubuntutoolkit.obb")
                if (!rootfs.exists() && obbFile.exists()) {
                    rootfs.mkdirs()
                    // 调用系统 tar 解压
                    val cmd = arrayOf("/system/bin/tar", "-xf", obbFile.absolutePath, "-C", rootfs.absolutePath)
                    val p = Runtime.getRuntime().exec(cmd)
                    p.waitFor()
                }
                runOnUiThread {
                    status.text = "正在启动 PRoot..."
                    startUbuntu(rootfs.absolutePath)
                }
            }.start()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            proc?.destroy(); proc = null
            status.text = "已停止"
        }
    }

    private fun startUbuntu(rootfsPath: String) {
        try {
            val nativeDir = applicationInfo.nativeLibraryDir
            val proot = "$nativeDir/libproot.so"
            val loader = "$nativeDir/libproot_loader.so"
            File("$nativeDir/libtalloc.so").copyTo(File("$nativeDir/libtalloc.so.2"), overwrite = true)

            val pb = ProcessBuilder(proot, "--link2symlink", "-0", "-r", rootfsPath, "-b", "/dev", "-b", "/proc", "-b", "/sys", "-w", "/root", "/bin/bash", "-c", "uname -a; sleep 3600")
            pb.environment()["PROOT_LOADER"] = loader
            pb.environment()["PROOT_NO_SECCOMP"] = "1"
            pb.redirectErrorStream(true)
            proc = pb.start()
            status.text = "Ubuntu 已启动"
        } catch (e: Exception) {
            status.text = "启动失败: ${e.message}"
        }
    }
}
