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

            val proot = "$nativeDir/libproot.so"
            val loader = "$nativeDir/libproot_loader.so"
            val talloc = "$nativeDir/libtalloc.so"
            File(talloc).copyTo(File("$nativeDir/libtalloc.so.2"), overwrite = true)

            val cmd = arrayOf(
                proot, "--link2symlink",
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
            pb.environment()["PROOT_LOADER"] = loader
            pb.environment()["PROOT_NO_SECCOMP"] = "1"
            pb.environment()["LD_LIBRARY_PATH"] = nativeDir
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
