package cn.example.consumablesManagement.logic.test

import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.Toasts.showToast
import com.fasterxml.jackson.databind.ObjectMapper
import java.sql.DriverManager
import java.util.concurrent.FutureTask

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesmanagement
 * @Author: BarryAllen
 * @Time: 2021/11/17 14:32 星期三
 * TODO:
 **************************/
class test {
}

fun main() {
    val mapper = ObjectMapper()
    val username = "llzzpp123"
    val password = "1234562"
    val uid = "5"
    val signUpTask =
        FutureTask(NetworkThread(username, password, url = NetworkSettings.SIGN_IN));
    val thread = Thread(signUpTask);
    thread.start();
    try {
        val body = mapper.readValue(
            signUpTask.get(),
            ResponseBody::class.java
        )
        println(if (body.code == 200) "注册成功 code = ${body.code} data = ${body.data}" else "注册失败")

    } catch (e: Exception) {
        e.printStackTrace();
    }
}