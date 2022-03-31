package cn.example.consumablesManagement.util.appUtil

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import cn.example.consumablesManagement.R

// [2021/11/29 8:56] 加载样式工具类
class Loading {

    private var loadingBuilder: AlertDialog.Builder? = null
    private var customBuilder: AlertDialog.Builder? = null
    private var loadingDialog: AlertDialog? = null
    private var customDialog: AlertDialog? = null

    fun loadingDialog(context: Context) {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog, null)
        loadingBuilder = AlertDialog.Builder(context, R.style.CustomDialog)
        loadingBuilder?.setView(view)
        loadingDialog = loadingBuilder?.create()
        loadingDialog?.setCancelable(false)
        loadingDialog?.show()
        loadingBuilder = null
    }

    fun cancelDialog() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    fun loadingCustomDialog(view: View, themeId: Int, block: View.() -> Unit) {
        customBuilder = AlertDialog.Builder(view.context, themeId)
        customBuilder?.setView(view)
        customDialog = customBuilder?.create()
        customDialog?.show()
        customBuilder = null
        view.block()
    }

    fun cancelCustomDialog() {
        customDialog?.dismiss()
        customDialog = null
    }
}


