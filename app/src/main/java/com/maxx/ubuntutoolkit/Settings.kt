package com.maxx.ubuntutoolkit

import android.content.Context

object Settings {
    private const val PREF = "ubuntutoolkit"
    const val KEY_ROOTFS = "rootfs_path"
    const val KEY_LOG = "log_path"
    const val KEY_VERBOSE = "verbose"

    fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun rootfs(ctx: Context): String =
        prefs(ctx).getString(KEY_ROOTFS, "${ctx.filesDir.absolutePath}/ubuntu")!!

    fun log(ctx: Context): String =
        prefs(ctx).getString(KEY_LOG, "${ctx.filesDir.absolutePath}/prout.log")!!

    fun verbose(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_VERBOSE, false)
}
