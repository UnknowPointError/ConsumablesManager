package cn.example.consumablesManagement.logic.model.response

import cn.example.consumablesManagement.logic.model.entity.ConsumablesSearchData

data class ConsumableSearchBody (val code: Int, val consumablesSearchList : ArrayList<ConsumablesSearchData>)
