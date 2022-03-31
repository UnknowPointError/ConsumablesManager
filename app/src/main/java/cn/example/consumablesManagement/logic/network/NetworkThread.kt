package cn.example.consumablesManagement.logic.network

import cn.example.consumablesManagement.util.ktUtil.TryCatchUtil
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable


/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/logic/network
 * @Time: 2021 13:10 / 11月
 * @Author: BarryAllen
 * TODO: 发送网络请求的异步的线程类
 **************************/
// @formatter:off
class NetworkThread(
    private val userName: String = "",
    private val passWord: String = "",
    private val uid: String = "",
    private val csaUserName: String = "",
    private val csaName: String = "",
    private val csaCount: String = "",
    private val rowMinLimit : String = "",
    private val time: String = "",
    private val url: String
) : Callable<String> {
1
    override fun call(): String {
        var result: String = ""
        TryCatchUtil.tryCatch({
            val connection = URL(url).openConnection() as HttpURLConnection
            val utf8 = StandardCharsets.UTF_8.toString()
            val data = "username=${URLEncoder.encode(userName, utf8)
            }&password=${URLEncoder.encode(passWord, utf8)
            }&uid=${URLEncoder.encode(uid, utf8)
            }&csaUserName=${URLEncoder.encode(csaUserName,utf8)
            }&csaName=${URLEncoder.encode(csaName, utf8)
            }&csaCount=${URLEncoder.encode(csaCount, utf8)
            }&rowMinLimit=${URLEncoder.encode(rowMinLimit,utf8)
            }&time=${URLEncoder.encode(time,utf8)}"
            connection.connectTimeout = 8000
            connection.readTimeout = 8000
            connection.requestMethod = "POST"
            connection.doInput = true
            connection.doOutput = true
            connection.outputStream.write(data.toByteArray(StandardCharsets.UTF_8))
            val bytes = ByteArray(1024)
            val len = connection.inputStream.read(bytes)
            result = String(bytes, 0, len, StandardCharsets.UTF_8)
        }, {
            result = ""
        })
        return result
    }
}