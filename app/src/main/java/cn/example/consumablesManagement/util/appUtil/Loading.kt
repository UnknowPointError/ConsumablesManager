package cn.example.consumablesManagement.util.appUtil

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import cn.example.consumablesManagement.R

// [2021/11/29 8:56] 加载样式工具类
class Loading {

    private var builder: AlertDialog.Builder? = null
    private var alertDialog: AlertDialog? = null
    private var alertBuilder: AlertDialog.Builder? = null
    private var dialog: AlertDialog? = null

    fun reloading(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        builder = AlertDialog.Builder(context, R.style.CustomDialog)
        builder?.setView(view)
        alertDialog = builder?.create()
        alertDialog?.setCancelable(false)
        alertDialog?.show()
    }

    fun unloading() = alertDialog?.dismiss()
    fun isloading() = alertDialog?.isShowing

    fun reloadUI(view: View, themeId: Int, block: View.() -> Unit) {
        alertBuilder = AlertDialog.Builder(view.context, themeId)
        alertBuilder?.setView(view)
        dialog = alertBuilder?.create()
        dialog?.show()
        view.block()
    }

    fun unloadUI() = dialog?.dismiss()
}


