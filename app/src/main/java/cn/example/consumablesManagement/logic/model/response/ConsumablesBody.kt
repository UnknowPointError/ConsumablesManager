package cn.example.consumablesManagement.logic.model.response

import cn.example.consumablesManagement.logic.model.entity.ConsumablesData

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/logic/model
 * @Time: 2021 19:38 / 12月
 * @Author: BarryAllen
 * TODO: 耗材响应格式
 **************************/
data class ConsumablesBody(val code: Int,val consumablesList: ArrayList<ConsumablesData>? = null)