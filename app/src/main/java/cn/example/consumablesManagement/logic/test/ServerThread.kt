package cn.example.consumablesManagement.logic.test

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement
 * @Author: BarryAllen
 * @Time: 2021/11/17 12:18 星期三
 * TODO:
 **************************/
class ServerThread(private val socket: Socket) : Thread() {
    override fun run() {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(InputStreamReader(socket.getInputStream()));
            val line: String = reader.readLine()
            println("来自客户端的数据：$line")
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace();
        }
    }

    private fun fire(block: () -> Unit) {
        try {
            block()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun main() {
    val server = ServerSocket(19300)
    while (true) {
        val socket = server.accept()
        val serverThread = ServerThread(socket)
        serverThread.start()
    }
}