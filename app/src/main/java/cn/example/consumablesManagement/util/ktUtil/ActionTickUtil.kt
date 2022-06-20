package cn.example.consumablesManagement.util.ktUtil

object ActionTickUtil {

    var networkTickTag = false
    var actionTickTag = false

    // [2021/12/12 19:35] 用于解决事件内多次响应处理过程,使用此函数应该避免内部开启线程
    inline fun actionInterval(block: () -> Unit) {
        if (!actionTickTag) {
            actionTickTag = true
            block()
            actionTickTag = false
        }
    }

    inline fun networkInterval(block: () -> Unit) {
        if (!networkTickTag) {
            networkTickTag = true
            block()
        }
    }
}