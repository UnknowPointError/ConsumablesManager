package cn.example.consumablesManagement.logic.model.entity


data class ConsumablesSearchData(
    val userName: String,
    val csaUID: Int,
    val csaName: String,
    val csaCount: Int,
    val time: String
)