package cn.example.consumablesManagement.logic.network

import cn.example.consumablesManagement.util.TRYCATCH
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
class NetworkThread(
    private val userName: String = "",
    private val passWord: String = "",
    private val uid: String = "",
    private val csaName: String = "",
    private val csaCount: String = "",
    private val url: String
) : Callable<String> {

    override fun call(): String {
        TRYCATCH.tryFunc({
            val connection = URL(url).openConnection() as HttpURLConnection
            // @formatter:off
            val data = "username=${URLEncoder.encode(userName, StandardCharsets.UTF_8.toString())
            }&password=${URLEncoder.encode(passWord, StandardCharsets.UTF_8.toString())
            }&uid=${URLEncoder.encode(uid, StandardCharsets.UTF_8.toString())
            }&csaName=${URLEncoder.encode(csaName, StandardCharsets.UTF_8.toString())
            }&csaCount=${URLEncoder.encode(csaCount, StandardCharsets.UTF_8.toString())
            }"
            // @formatter:on
            connection.requestMethod = "POST"
            connection.doInput = true
            connection.doOutput = true
            connection.outputStream.write(data.toByteArray(StandardCharsets.UTF_8))
            val bytes = ByteArray(1024)
            val len = connection.inputStream.read(bytes)
            return String(bytes, 0, len, StandardCharsets.UTF_8)
        }, {
            return ""
        })
        return ""
    }
}