package com.maxx.ubuntutoolkit

import android.content.Context

object Settings {
    private const val PREF = "ubuntutoolkit"
    const val KEY_ROOTFS = "rootfs_path"
    const val KEY_LOG = "log_path"
    const val KEY_VERBOSE = "verbose"
    const val KEY_AI_BASE_URL = "ai_base_url"
    const val KEY_AI_API_KEY = "ai_api_key"
    const val KEY_AI_MODEL = "ai_model"

    fun prefs(ctx: Context) = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)

    fun rootfs(ctx: Context): String =
        prefs(ctx).getString(KEY_ROOTFS, "${ctx.filesDir.absolutePath}/ubuntu")!!

    fun log(ctx: Context): String =
        prefs(ctx).getString(KEY_LOG, "${ctx.filesDir.absolutePath}/prout.log")!!

    fun verbose(ctx: Context): Boolean = prefs(ctx).getBoolean(KEY_VERBOSE, false)

    fun aiBaseUrl(ctx: Context): String =
        prefs(ctx).getString(KEY_AI_BASE_URL, "https://api.openai.com/v1") ?: "https://api.openai.com/v1"

    fun aiApiKey(ctx: Context): String =
        prefs(ctx).getString(KEY_AI_API_KEY, "") ?: ""

    fun aiModel(ctx: Context): String =
        prefs(ctx).getString(KEY_AI_MODEL, "gpt-4o-mini") ?: "gpt-4o-mini"

    fun aiConfigured(ctx: Context): Boolean = aiApiKey(ctx).isNotBlank()
}
