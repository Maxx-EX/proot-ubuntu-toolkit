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

            // nativeLibraryDir 只读，不能写 libtalloc.so.2；
            // 但 filesDir 不可 exec（Android 10+ W^X），proot/loader 必须留在 nativeLibraryDir。
            // 只把 libtalloc.so.2 复制到可写目录，LD_LIBRARY_PATH 指过去。
            val workDir = File(ctx.filesDir, "native").apply { mkdirs() }
            File(nativeDir, "libtalloc.so").copyTo(File(workDir, "libtalloc.so.2"), overwrite = true)

            val proot = "$nativeDir/libproot.so"
            val loader = "$nativeDir/libproot_loader.so"

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
            pb.environment()["LD_LIBRARY_PATH"] = "$nativeDir:${workDir.absolutePath}"
            pb.environment()["OPENAI_BASE_URL"] = Settings.aiBaseUrl(ctx)
            pb.environment()["OPENAI_API_KEY"] = Settings.aiApiKey(ctx)
            pb.environment()["OPENAI_MODEL"] = Settings.aiModel(ctx)
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
