package cn.example.consumablesManagement.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import cn.example.consumablesManagement.databinding.RegisterActivityBinding
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import cn.smssdk.gui.RegisterPage
import com.fasterxml.jackson.databind.ObjectMapper
import com.mob.MobSDK
import java.lang.StringBuilder
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showSnackBar
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil.connectHttp

class RegisterActivity : AppCompatActivity() {


    private var count = 0
    private val mapper = ObjectMapper()
    private val mBinding by lazy { RegisterActivityBinding.inflate(layoutInflater) }
    private val main = this
    private var eventHandler = EventHandler()
    private val loading = Loading()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        SMSSDK.unregisterAllEventHandler()
        SMSSDK.unregisterEventHandler(eventHandler)
    }

    // [2021/12/13 16:11] Activity 初始化UI界面及数据
    private fun initUI() = mBinding.apply {
        supportActionBar?.hide()
        registerUser.addTextChangedListener { actionInterval { setRegisterUserStatus() } }
        registerPwd.addTextChangedListener { actionInterval { setRegisterPwdStatus() } }
        registerPwdCheck.addTextChangedListener { actionInterval { setRegisterPwdCheckStatus() } }
        registerBtn.setOnClickListener { actionInterval { register() } }
    }

    // [2021/12/13 16:18] 设置账号的实时状态
    private fun setRegisterUserStatus() = mBinding.apply {
        if (registerUser.length() > 0) {
            val stringBuilder = StringBuilder()
            registerUser.text.forEach { if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z') || (it == '@' || it == '.')) { stringBuilder.append(it) } }
            registerUser.setText(stringBuilder)
            registerUser.setSelection(stringBuilder.length)
        }
    }

    // [2021/12/13 16:19] 设置密码的实时状态
    private fun setRegisterPwdStatus() = mBinding.apply {
        if (registerPwd.length() > 0) {
            val stringBuilder = StringBuilder()
            registerPwd.text.forEach { if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) { stringBuilder.append(it) } }
            registerPwd.setText(stringBuilder)
            registerPwd.setSelection(stringBuilder.length)
        }
    }

    // [2021/12/13 16:19] 设置密码检查的实时状态
    private fun setRegisterPwdCheckStatus() = mBinding.apply {
        if (registerPwdCheck.length() > 0) {
            val stringBuilder = StringBuilder()
            registerPwdCheck.text.forEach { if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) { stringBuilder.append(it) } }
            registerPwdCheck.setText(stringBuilder)
            registerPwdCheck.setSelection(stringBuilder.length)
        }
    }

    // [2021/12/13 16:20] 注册
    private fun register() = mBinding.apply {
        val judgeResult = judgeText()
        if (judgeResult[1] == false) root.showSnackBar("${judgeResult[0]}")
        else registerJudge()
    }

    private fun judgeText(): ArrayList<Any> {
        mBinding.apply {
            return if (registerUser.length() < 6 || registerUser.length() > 16) { arrayListOf("设置的账号不符合规范！", false)
            } else if (registerPwd.length() < 6 || registerUser.length() > 16) { arrayListOf("设置的密码不符合规范！", false)
            } else if (registerPwdCheck.length() < 6 || registerPwdCheck.length() > 16) { arrayListOf("二次确认所输入的密码不符合规范！", false)
            } else if (registerPwd.text.toString() != registerPwdCheck.text.toString()) {
                registerPwdCheck.text = null
                arrayListOf("两次输入的密码不匹配", false)
            } else if (registerId.length() < 1) { arrayListOf("设置的Uid不符合规范！", false)
            } else { arrayListOf("", true)
            }
        }
    }

    private fun registerJudge() = mBinding.apply {
        loading.loadingDialog(context = main)
        val networkThread = NetworkThread(registerUser.text.toString(), registerPwd.text.toString(), registerId.text.toString(), url = AppData.SIGN_UP)
        connectHttp<ResponseBody>(4000,true,this@RegisterActivity,networkThread, {
            when (code) {
                200 -> { mBinding.root.showSnackBar("注册成功！") }
                500 -> { mBinding.root.showSnackBar("注册失败") }
                else -> { mBinding.root.showSnackBar("账号已存在，请换一个试试。") }
            }
            loading.cancelDialog()
        }) {
            loading.cancelDialog()
            mBinding.root.showSnackBar("网络异常！")
        }
    }

    private fun showRegisterUI() {
        val page = RegisterPage()
        MobSDK.submitPolicyGrantResult(true, null)
        page.setTempCode(null)
        page.setRegisterCallback(eventHandler)
        eventHandler = object : EventHandler() {
            override fun afterEvent(event: Int, result: Int, data: Any?) {
                if (event == SMSSDK.EVENT_VERIFY_LOGIN) {
                    Log.e("event", "result  : $result")

                }
                if (result == SMSSDK.RESULT_COMPLETE) {
                    val phoneMap: HashMap<String, Any> = data as HashMap<String, Any>
                    // val country = phoneMap["country"]
                    // val phone = phoneMap["phone"]
                    Log.e("event", "afterEvent: $event")
                }
            }
        }
        page.show(main)
    }
}