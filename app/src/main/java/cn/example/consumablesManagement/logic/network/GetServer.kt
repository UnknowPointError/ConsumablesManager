package cn.example.consumablesManagement.logic.network

import android.util.Log
import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement
 * @Author: BarryAllen
 * @Time: 2021/11/17 12:34 星期三
 * TODO: 连接服务器
 **************************/
object GetServer {

    fun connection(content: ArrayList<String>) {
        try {
            val socket = Socket("120.79.132.118", 38381)
            socket.soTimeout = 10000
            print("连接成功")
            val pw = PrintWriter(socket.getOutputStream())
            if (content.size > 0)
                if (content[content.size - 1] != "\n")
                    content.add("\n")
            content.forEach {
                if (it.length > 2 && it.contains("\n")) {
                    val subText = it.split("\n")
                    subText.forEach {
                        pw.write(it)
                        pw.flush()
                    }
                } else {
                    pw.write(it)
                    pw.flush()
                }
                pw.flush()
            }
            pw.close()
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }
}

