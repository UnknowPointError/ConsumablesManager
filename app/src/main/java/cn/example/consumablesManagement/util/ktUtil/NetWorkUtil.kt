package cn.example.consumablesManagement.util.ktUtil

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.os.Message
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showToast
import cn.example.consumablesManagement.util.ktUtil.TryCatchUtil.tryCatch
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread


// [2021/12/12 19:47] 网络连接工具类
object NetWorkUtil {

    lateinit var i: Loading

    // crossinline不支持lambda函数内进行非局部的返回
    inline fun <reified T> connectHttp(
        delay: Long = 0,
        openEndBlock: Boolean = false,
        activity: Activity,
        networkThread: NetworkThread,
        crossinline bodyBlock: T.() -> Unit,
        crossinline endBlock: () -> Unit = {},
    ) {
        var tagNetWorkError = true
        val task = FutureTask(networkThread)
        Thread(task).start()
        tryCatch({
            thread {
                task.get().apply {
                    if (isNotBlank()) {
                        val body = Gson().fromJson(this, T::class.java)
                        activity.runOnUiThread {
                            body.bodyBlock()
                            tagNetWorkError = false
                        }
                    }
                }
            }
        }, finallyBlock = {
            if (openEndBlock) thread {
                if (delay != 0L)
                    Thread.sleep(delay)
                if (tagNetWorkError)
                    activity.runOnUiThread { endBlock() }
            }
        })
    }
    inline fun <reified T> connectServer(
        activity: Activity,
        ifNeedLoading: Boolean,
        networkThread: NetworkThread,
        crossinline bodyBlock: T.() -> Unit,
        crossinline errorBlock: () -> Unit
    ) {
        val task = FutureTask(networkThread)
        val loading by lazy { Loading() }
        val errorJob = CoroutineScope(Job()).launch(start = CoroutineStart.LAZY) {
            delay(4000)
            if (ifNeedLoading) loading.cancelDialog()
            activity.runOnUiThread { App.context.showToast("网络异常") }
            errorBlock()
            ActionTickUtil.networkTickTag = false
        }
        Thread(task).start()
        tryCatch({
            if (ifNeedLoading) loading.loadingDialog(activity)
            thread {
                errorJob.start()
                task.get().apply {
                    if (isNotBlank()) {
                        val body = Gson().fromJson(this, T::class.java)
                        body.bodyBlock()
                        loading.cancelDialog()
                        errorJob.cancel()
                        ActionTickUtil.networkTickTag = false
                    }
                }
            }
        })
    }
}
