package com.maxx.ubuntutoolkit

import android.content.Context
import android.util.Log
import java.io.File

class ProotLauncher(private val ctx: Context) {
    private var proc: Process? = null

    fun startUbuntu(): Boolean {
        return try {
            val nativeDir = ctx.applicationInfo.nativeLibraryDir
            val rootfs = Settings.rootfs(ctx)
            File(rootfs).mkdirs()

            // nativeLibraryDir 是只读的（/data/app/.../lib/arm64/），
            // 不能往里写 libtalloc.so.2。把所有原生二进制复制到可写目录再运行。
            val workDir = File(ctx.filesDir, "native").apply { mkdirs() }
            val proot = File(workDir, "proot")
            val loader = File(workDir, "libproot_loader.so")
            val talloc = File(workDir, "libtalloc.so.2")

            File(nativeDir, "libproot.so").copyTo(proot, overwrite = true)
            File(nativeDir, "libproot_loader.so").copyTo(loader, overwrite = true)
            File(nativeDir, "libtalloc.so").copyTo(talloc, overwrite = true)
            proot.setExecutable(true, false)

            val cmd = arrayOf(
                proot.absolutePath, "--link2symlink",
                "-0", "-r", rootfs,
                "-b", "/dev", "-b", "/proc", "-b", "/sys",
                "-b", "${ctx.filesDir.absolutePath}:/home/developer",
                "-w", "/root",
                "/bin/bash", "-c",
                "export ANDROID_HOME=/opt/android-sdk; " +
                "export ANDROID_SDK_ROOT=/opt/android-sdk; " +
                "export PATH=\$PATH:/opt/gradle-8.7/bin:/opt/kotlinc/bin:/opt/android-sdk/cmdline-tools/latest/bin:/opt/android-sdk/platform-tools; " +
                "echo '=== Ubuntu Toolkit ==='; uname -a; java -version; gradle -v | head -5; " +
                "sleep 3600"
            )

            val pb = ProcessBuilder(*cmd)
            pb.environment()["PROOT_LOADER"] = loader.absolutePath
            pb.environment()["PROOT_NO_SECCOMP"] = "1"
            pb.environment()["LD_LIBRARY_PATH"] = workDir.absolutePath
            pb.redirectErrorStream(true)
            proc = pb.start()
            Thread {
                val logFile = File(Settings.log(ctx)).apply { parentFile?.mkdirs() }
                proc?.inputStream?.bufferedReader()?.forEachLine { line ->
                    Log.i("ProotOut", line)
                    if (Settings.verbose(ctx)) logFile.appendText("$line\n")
                }
            }.start()
            true
        } catch (e: Exception) {
            Log.e("Proot", "start failed", e); false
        }
    }

    fun stop() { proc?.destroy(); proc = null }
}
