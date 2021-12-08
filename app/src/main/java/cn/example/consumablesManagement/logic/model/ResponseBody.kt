package cn.example.consumablesManagement.logic.model

class ResponseBody(val code: Int, val data: Any) {
    constructor() : this(0, "") {

    }
}