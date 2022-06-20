package cn.example.consumablesManagement.util.appUtil

import android.content.Context
import android.content.Intent
import cn.example.consumablesManagement.App

// [2021/12/12 19:43] 启动Activity
object StartActivityUtil {

    inline fun <reified T> Context.startActivity(block: Intent.() -> Unit) {
        val intent = Intent(this, T::class.java)
        intent.block()
        this.startActivity(intent)
    }
}