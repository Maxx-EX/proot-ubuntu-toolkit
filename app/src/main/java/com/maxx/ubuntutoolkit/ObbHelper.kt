package com.maxx.ubuntutoolkit

import android.content.Context
import android.os.StorageManager
import android.os.storage.OnObbStateChangeListener
import android.util.Log
import java.io.File
import java.util.concurrent.CountDownLatch

object ObbHelper {
    fun mount(ctx: Context, obbFile: File): String? {
        val sm = ctx.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        var mounted: String? = null
        val latch = CountDownLatch(1)
        sm.mountObb(obbFile.absolutePath, null, object : OnObbStateChangeListener() {
            override fun onObbStateChange(path: String?, state: Int) {
                if (state == MOUNTED) mounted = sm.getMountedObbPath(obbFile.absolutePath)
                latch.countDown()
            }
        })
        latch.await()
        Log.i("OBB", "mounted: $mounted")
        return mounted
    }
}
