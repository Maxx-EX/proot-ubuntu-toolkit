package com.maxx.ubuntutoolkit

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var status: TextView
    private var proc: Process? = null

    // 注册文件选择器
    private val pickObbLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                status.text = "正在准备解压 OBB，请稍候..."
                Thread {
                    try {
                        // 1. 先把选中的文件拷贝到应用的缓存目录
                        val cacheTar = File(cacheDir, "temp_obb.tar")
                        contentResolver.openInputStream(uri)?.use { input ->
                            FileOutputStream(cacheTar).use { output ->
                                input.copyTo(output)
                            }
                        }

                        // 2. 准备解压路径
                        val rootfs = File(filesDir, "ubuntu")
                        if (rootfs.exists()) rootfs.deleteRecursively()
                        rootfs.mkdirs()

                        // 3. 调用系统 tar 解压
                        runOnUiThread { status.text = "正在解压 Ubuntu 系统 (约需1-3分钟)..." }
                        val cmd = arrayOf("/system/bin/tar", "-xf", cacheTar.absolutePath, "-C", rootfs.absolutePath)
                        val p = Runtime.getRuntime().exec(cmd)
                        p.waitFor()

                        // 4. 清理临时文件
                        cacheTar.delete()

                        runOnUiThread {
                            status.text = "正在启动 PRoot..."
                            startUbuntu(rootfs.absolutePath)
                        }
                    } catch (e: Exception) {
                        runOnUiThread { status.text = "解压失败: ${e.message}" }
                    }
                }.start()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.statusText)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            val rootfs = File(filesDir, "ubuntu")
            // 如果已经解压过了，直接启动
            if (rootfs.exists() && rootfs.list()?.isNotEmpty() == true) {
                status.text = "正在启动 PRoot..."
                startUbuntu(rootfs.absolutePath)
            } else {
                // 如果没解压，弹出文件选择器
                status.text = "请选择你下载的 OBB 文件"
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.type = "*/*"
                pickObbLauncher.launch(intent)
            }
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            proc?.destroy(); proc = null
            status.text = "已停止"
        }
    }

    private fun startUbuntu(rootfsPath: String) {
        try {
            val nativeDir = applicationInfo.nativeLibraryDir
            // nativeLibraryDir 是只读的（/data/app/.../lib/arm64/），
            // 不能往里写 libtalloc.so.2。把所有原生二进制复制到可写目录再运行。
            val workDir = File(filesDir, "native").apply { mkdirs() }
            val proot = File(workDir, "proot")
            val loader = File(workDir, "libproot_loader.so")
            val talloc = File(workDir, "libtalloc.so.2")

            File(nativeDir, "libproot.so").copyTo(proot, overwrite = true)
            File(nativeDir, "libproot_loader.so").copyTo(loader, overwrite = true)
            File(nativeDir, "libtalloc.so").copyTo(talloc, overwrite = true)
            proot.setExecutable(true, false)

            val pb = ProcessBuilder(
                proot.absolutePath, "--link2symlink", "-0", "-r", rootfsPath,
                "-b", "/dev", "-b", "/proc", "-b", "/sys",
                "-w", "/root", "/bin/bash", "-c", "uname -a; sleep 3600"
            )
            pb.environment()["PROOT_LOADER"] = loader.absolutePath
            pb.environment()["PROOT_NO_SECCOMP"] = "1"
            pb.environment()["LD_LIBRARY_PATH"] = workDir.absolutePath
            pb.redirectErrorStream(true)
            proc = pb.start()
            status.text = "Ubuntu 已启动"
        } catch (e: Exception) {
            status.text = "启动失败: ${e.message}"
        }
    }
}
