package com.maxx.ubuntutoolkit

import android.content.Context
import android.util.Log
import java.io.File

class ProotLauncher(private val ctx: Context) {
    private var proc: Process? = null

    fun startUbuntu(): Boolean {
        return try {
            val nativeDir = ctx.applicationInfo.nativeLibraryDir
            val filesDir = ctx.filesDir.absolutePath
            val rootfs = "$filesDir/ubuntu"
            File(rootfs).mkdirs()

            val proot = "$nativeDir/libproot.so"
            val loader = "$nativeDir/libproot_loader.so"
            val talloc = "$nativeDir/libtalloc.so"

            // Termux 的 proot 依赖 libtalloc.so.2，运行时复制一份
            val talloc2 = "$nativeDir/libtalloc.so.2"
            File(talloc).copyTo(File(talloc2), overwrite = true)

            val cmd = arrayOf(
                proot, "--link2symlink",
                "-0", "-r", rootfs,
                "-b", "/dev", "-b", "/proc", "-b", "/sys",
                "-b", "$filesDir:/home",
                "-w", "/root",
                "/bin/bash", "-c", "echo PRoot OK; uname -a; gcc --version; sleep 3600"
            )

            val pb = ProcessBuilder(*cmd)
            pb.environment()["PROOT_LOADER"] = loader
            pb.environment()["PROOT_NO_SECCOMP"] = "1"
            pb.environment()["LD_LIBRARY_PATH"] = nativeDir
            pb.redirectErrorStream(true)
            proc = pb.start()
            Thread { proc?.inputStream?.bufferedReader()?.forEachLine { Log.i("ProotOut", it) } }.start()
            true
        } catch (e: Exception) {
            Log.e("Proot", "start failed", e)
            false
        }
    }

    fun stop() { proc?.destroy(); proc = null }
}
