package cn.example.consumablesManagement.logic.test

import java.io.IOException
import java.io.PrintWriter
import java.net.Socket

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/logic
 * @Time: 2021 11:09 / 11月
 * @Author: BarryAllen
 * TODO: Lianjie
 **************************/
class conne {


}

fun main() {
    val content = arrayListOf("#q,","7\n")
    while (true) {
        println("fason")
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