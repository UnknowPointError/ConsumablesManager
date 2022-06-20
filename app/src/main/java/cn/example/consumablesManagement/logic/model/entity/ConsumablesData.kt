package cn.example.consumablesManagement.logic.model.entity

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/logic/model
 * @Time: 2021 15:23 / 12月
 * @Author: BarryAllen
 * TODO: 耗材转换格式
 **************************/
data class ConsumablesData(
    val csaUID: Int,
    val csaName: String,
    val csaCount: Int,
    val path: String,
    val time: String
)