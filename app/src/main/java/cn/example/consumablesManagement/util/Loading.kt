package cn.example.consumablesManagement.util

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.util.Loading.reloading

// @TODO: DataTime = 2021/11/29 8:56
// @TODO: 加载样式工具类
object Loading {

    private var builder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null

    private var alertBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null

    fun Context.reloading() {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog, null)
        builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder?.setView(view)
        alertDialog = builder?.create()
        alertDialog?.setCancelable(false)
        alertDialog?.show()
    }

    fun unloading() = alertDialog?.dismiss()

    fun View.reloadUI(themeId: Int, block: View.() -> Unit) {
        alertBuilder = AlertDialog.Builder(this.context, themeId)
        alertBuilder?.setView(this)
        dialog = alertBuilder?.create()
        dialog?.show()
        this.block()
    }

    fun unloadUI() = dialog?.dismiss()
}


