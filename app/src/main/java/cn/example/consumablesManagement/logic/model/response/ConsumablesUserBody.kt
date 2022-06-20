package cn.example.consumablesManagement.logic.model.response

import cn.example.consumablesManagement.logic.model.entity.ConsumablesUserData


data class ConsumablesUserBody(
    val code: Int,
    val consumablesUserData: ArrayList<ConsumablesUserData>
)