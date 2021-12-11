package cn.example.consumablesManagement.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.TextView
import android.widget.Toast
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.R
import com.google.android.material.snackbar.Snackbar

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/util
 * @Time: 2021 1:36 / 11月
 * @Author: BarryAllen
 * TODO: 显示showToast＆SnackBar
 **************************/
object ShowUtil {

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