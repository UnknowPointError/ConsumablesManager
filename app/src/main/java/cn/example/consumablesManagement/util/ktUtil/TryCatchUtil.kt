package cn.example.consumablesManagement.util.ktUtil


// [2021/12/12 19:47] kotlin try Catch finally
object TryCatchUtil {

    inline fun tryCatch(tryBlock: () -> Unit, exceptionBlock: () -> Unit = {},finallyBlock: () -> Unit = {}) {
        try {
            tryBlock()
        } catch (e: Exception) {
            e.printStackTrace()
            exceptionBlock()
        } finally {
            finallyBlock()
        }
    }
}
