package cn.example.consumablesManagement.util.appUtil

import android.content.Context
import cn.example.consumablesManagement.App


/// [2021/12/12 19:40] sharedPreferences Util工具类 用于账号信息
object SPUtil {
    // @formatter:off
    private val sharedPreferences = App.context.getSharedPreferences("user", Context.MODE_PRIVATE)
    fun removeValue(key: String): Any = sharedPreferences.edit().remove(key).apply()
    fun putString(key: String, content: String) = sharedPreferences.edit()?.putString(key, content)?.apply()
    fun putBoolean(key: String, bool: Boolean) = sharedPreferences.edit()?.putBoolean(key, bool)?.apply()
    fun getString(key: String): String = sharedPreferences.getString(key, "").toString()
    fun getBoolean(key: String): Boolean = sharedPreferences.getBoolean(key, false)
}