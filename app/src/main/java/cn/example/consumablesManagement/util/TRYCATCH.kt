package cn.example.consumablesManagement.util

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/util
 * @Time: 2021 12:01 / 12月
 * @Author: BarryAllen
 * TODO: try catch
 **************************/
object TRYCATCH {
    inline fun tryFunc(tryBlock: () -> Unit, exceptionBlock: () -> Unit = {}) {
        try {
            tryBlock()
        } catch (e: Exception) {
            e.printStackTrace()
            exceptionBlock()
        }
    }
}