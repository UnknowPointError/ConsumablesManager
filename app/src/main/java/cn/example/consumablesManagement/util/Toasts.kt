package cn.example.consumablesManagement.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.*
import android.widget.TextView
import android.widget.Toast
import cn.example.consumablesManagement.R
import com.google.android.material.snackbar.Snackbar

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/util
 * @Time: 2021 1:36 / 11月
 * @Author: BarryAllen
 * TODO: 显示showToast
 **************************/
object Toasts {

    private var toast: Toast? = null
    private var snackbar: Snackbar? = null

    // @TODO: DataTime = 2021/11/19 1:39
    // @TODO: 显示showToast
    fun Context.showToast(text: String, displayTimeMode: Boolean = false) {
        if (toast != null)
            (toast ?: return).cancel()// 当toast为null时返回冒号后面的值，只有return则退出
        toast = if (!displayTimeMode)
            Toast.makeText(this, text, Toast.LENGTH_SHORT)
        else
            Toast.makeText(this, text, Toast.LENGTH_LONG)
        toast?.show()
    }

    fun View.showSnackBar(message: String, duration: Int = Snackbar.LENGTH_SHORT) {
        if (snackbar != null) {
            (snackbar ?: return).dismiss()
        }
        snackbar = Snackbar.make(this, message, duration)
        snackbar?.show()
    }
}