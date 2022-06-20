package cn.example.consumablesManagement.view.activity.viewModel

import android.app.Activity
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.example.consumablesManagement.logic.model.response.ConsumablesBody
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil
import cn.example.consumablesManagement.view.adapter.ApplyFragmentRvAdapter
import java.io.File
import java.lang.StringBuilder


class HomeViewModel : ViewModel() {

    var imageUri: Uri? = null
    var adapter: ApplyFragmentRvAdapter? = null
    var position: Int? = null
    var outputImage: File? = null
    val stringBuilder = StringBuilder()

    private val _authority = MutableLiveData("cn.example.consumablesManagement.view.fragment")
    val authority: LiveData<String> get() = _authority

    private val _code = MutableLiveData<Int>()
    val code: LiveData<Int> get() = _code

    private val _data = MutableLiveData<Any>()
    val data: LiveData<Any> get() = _data

    private val _responseBody = MutableLiveData<ResponseBody>()
    val responseBody: LiveData<ResponseBody> get() = _responseBody

    private val _initRvData = MutableLiveData<ConsumablesBody>()
    val initRvData: LiveData<ConsumablesBody> get() = _initRvData

    private val _loadingMoreData = MutableLiveData<ConsumablesBody>()
    val loadingMoreData: LiveData<ConsumablesBody> get() = _loadingMoreData

    // [2021/12/28 19:48]
    fun setResponseBody(
        ifNeedLoading: Boolean,
        activity: Activity,
        networkThread: NetworkThread,
    ) {
        NetWorkUtil.connectServer<ResponseBody>(
            activity,
            ifNeedLoading,
            networkThread,
            { _responseBody.postValue(this) },
            { _responseBody.postValue(ResponseBody(404)) }
        )
    }

    // [2021/12/30 13:26] 设置 网络请求耗材返回数据
    fun setInitRvBody(
        ifNeedLoading: Boolean,
        activity: Activity,
        networkThread: NetworkThread,
    ) {
        NetWorkUtil.connectServer<ConsumablesBody>(
            activity,
            ifNeedLoading,
            networkThread,
            { _initRvData.postValue(this) },
            { _initRvData.postValue(ConsumablesBody(404)) }
        )
    }

    fun setLoadingMoreBody(
        ifNeedLoading: Boolean,
        activity: Activity,
        networkThread: NetworkThread,
    ) {
        NetWorkUtil.connectServer<ConsumablesBody>(
            activity,
            ifNeedLoading,
            networkThread,
            { _initRvData.postValue(this) },
            { _initRvData.postValue(ConsumablesBody(404)) }
        )
    }
}