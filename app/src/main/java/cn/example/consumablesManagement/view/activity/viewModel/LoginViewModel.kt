package cn.example.consumablesManagement.view.activity.viewModel

import android.app.Activity
import android.text.Editable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.util.appUtil.SPUtil
import cn.example.consumablesManagement.util.ktUtil.AES
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil.connectServer
import cn.example.consumablesManagement.view.adapter.LoginRecyclerViewAdapter
import java.lang.StringBuilder

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/view/activity
 * @Time: 2021 14:24 / 12月
 * @Author: BarryAllen
 * TODO: Login ViewModel
 **************************/
class LoginViewModel : ViewModel() {

    val remeberUser = "RemeberUser"
    val remeberPwd = "RemeberPwd"
    val remeberLogin = "RemeberAutoLogin"
    val userName = "username"
    val passWord = "password"
    val lastLoginUser = "LastLoginUser"
    val userSplitContent = mutableListOf<String>()
    val userListData = ArrayList<LoginRecyclerViewAdapter.User>()
    var dropDownListStatus = false

    private val _responseBody = MutableLiveData<ResponseBody>()
    val responseBody: LiveData<ResponseBody> get() = _responseBody

    private val _drowDownList = MutableLiveData<Boolean>()
    val drowDownList: LiveData<Boolean> get() = _drowDownList

    private val _userContent = MutableLiveData<StringBuilder>()
    val userContent: LiveData<StringBuilder> get() = _userContent

    private val _pwdContent = MutableLiveData<StringBuilder>()
    val pwdContent: LiveData<StringBuilder> get() = _pwdContent

    private val _newUserName = MutableLiveData<String>()
    val newUserName: LiveData<String> get() = _newUserName

    private val _newPassWord = MutableLiveData<String>()
    val newPassWord: LiveData<String> get() = _newPassWord

    // [2021/12/28 19:48] 设置 网络响应数据
    fun setResponseData(activity: Activity,networkThread: NetworkThread) {
        connectServer<ResponseBody>(activity,true, networkThread,
            { _responseBody.postValue(this) },
            { _responseBody.postValue(ResponseBody(AppData.ERROR_CODE)) }
        )
    }

    // [2021/12/28 19:48] 保存SP存储的账号
    fun toSaveUser(userName: String) {
        val oldUserName = SPUtil.getString(this.userName)
        if (oldUserName.isBlank()) SPUtil.putString(this.userName, userName)
        else {
            val containUserName = oldUserName.contains(userName)
            if ( containUserName && userName.length != oldUserName.length) {
                val index = oldUserName.indexOf(userName)
                var newUserName = oldUserName.removeRange(index,index + userName.length)
                if (newUserName.endsWith(',')) {
                    newUserName = newUserName.removeSuffix(",")
                }
                if (newUserName.startsWith(',')) {
                    newUserName = newUserName.removePrefix(",")
                }
                SPUtil.removeValue(this.userName)
                SPUtil.putString(this.userName, "$userName,$newUserName")
            } else if (!containUserName) {
                SPUtil.removeValue(this.userName)
                SPUtil.putString(this.userName, "$userName,$oldUserName")
            }
        }
    }

    // [2021/12/28 19:47] 删除SP存储的账号并重新存储
    fun toRemoveUser(userName: String) {
        _drowDownList.value = true
        val user = SPUtil.getString(this.userName)
        when {
            user.contains(",$userName") -> {
                SPUtil.removeValue(this.userName)
                SPUtil.putString(this.userName, user.removeSuffix(",$userName"))
            }
            user.contains("$userName,") -> {
                SPUtil.removeValue(this.userName)
                SPUtil.putString(this.userName, user.removePrefix("$userName,"))
            }
            user.contains(userName) -> {
                SPUtil.removeValue(this.userName)
                SPUtil.putString(this.userName, user.removePrefix(userName))
                _drowDownList.value = false
            }
        }
    }

    // [2021/12/28 19:47] 保存SP存储的密码
    fun toSavePwd(userName: String, passWord: String) {
        if (SPUtil.getString("${this.passWord}$passWord") == "") {
            SPUtil.putString("${this.passWord}$userName", passWord)
        }
    }

    // [2021/12/28 19:47] 删除SP存储的密码
    fun toRemovePwd(userName: String) {
        SPUtil.removeValue("${this.passWord}$userName")
    }

    // [2021/12/28 20:03] 设置用户输入框的数据
    fun setUserEditTextStatus(userName: Editable) {
        val stringBuilder = StringBuilder()
        userName.forEach {
            if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z') || (it == '@' || it == '.')) {
                stringBuilder.append(it)
            }
        }
        _userContent.value = stringBuilder
    }

    // [2021/12/29 13:17] 设置密码输入框的数据
    fun setPwdEditTextStatus(passWord: Editable) {
        val stringBuilder = StringBuilder()
        passWord.forEach {
            if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
                stringBuilder.append(it)
            }
        }
        _pwdContent.value = stringBuilder
    }

    fun getSpData(
        userName: String,
        aes: AES,
        isRemeberUser: Boolean,
        isRemeberPwd: Boolean
    ) : Boolean {
        if (isRemeberUser) {
            if (userName.isNotBlank()) {
                if (userName.contains(",")) {
                    _newUserName.value = userName.substring(0, userName.indexOf(','))
                } else {
                    _newUserName.value = userName
                }
                val pwd = SPUtil.getString("${this.passWord}${newUserName.value}")
                if (isRemeberPwd && pwd.isNotBlank()) {
                    val i = newUserName.value
                    val j = newUserName.value
                    val passWord = aes.decrypt(pwd , aes.loadKeyAES(AppData.AES_KEY))
                    if (passWord.isNotBlank()) {
                        _newPassWord.value = passWord
                    }
                    return true
                }
                return false
            }
            return false
        }
        return false
    }

}