package cn.example.consumablesManagement.util.appUtil

import android.content.Context
import android.view.*
import android.widget.Toast
import cn.example.consumablesManagement.App
import com.google.android.material.snackbar.Snackbar

/// [2021/12/12 19:42] 显示showToast＆SnackBar
object TipsUtil {

    private var toast: Toast? = null
    private var snackbar: Snackbar? = null

    // [2021/11/19 1:39] @TODO: 显示showToast
    fun Context.showToast(text: String, displayTimeMode: Boolean = false) {
        toast?.cancel()
        toast = if (!displayTimeMode)
            Toast.makeText(App.context, text, Toast.LENGTH_SHORT)
        else
            Toast.makeText(App.context, text, Toast.LENGTH_LONG)
        toast?.show()
    }

    // [2021/12/9 16:08] @TODO: 显示SnackBar
    fun View.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        snackbar?.dismiss()
        snackbar = Snackbar.make(this, message, duration)
        snackbar?.show()
    }
}