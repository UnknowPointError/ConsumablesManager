package cn.example.consumablesManagement.view.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.widget.addTextChangedListener
import cn.example.consumablesManagement.databinding.RegisterActivityBinding
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.Loading.reloading
import cn.example.consumablesManagement.util.Loading.unloading
import cn.smssdk.EventHandler
import cn.smssdk.SMSSDK
import cn.smssdk.gui.RegisterPage
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import com.mob.MobSDK
import java.lang.StringBuilder
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread
import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.util.Toasts.showSnackBar

class RegisterActivity : AppCompatActivity() {

    inner class RegisterManager {

        var count = 0
        private val mapper = ObjectMapper()

        fun initUI() {
            supportActionBar?.hide()
        }

        fun initComponent() = mBinding.apply {
            registerUser.addTextChangedListener { registerUser() }
            registerPwd.addTextChangedListener { registerPwd() }
            registerPwdCheck.addTextChangedListener { registerPwdCheck() }
            registerBtn.setOnClickListener { registerBtn() }
        }

        private fun registerUser() = mBinding.apply {
            if (registerUser.length() > count) {
                val stringBuilder = StringBuilder()
                registerUser.text.forEach {
                    if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z') || (it == '@' || it == '.')) {
                        stringBuilder.append(it)
                    }
                }
                count = stringBuilder.length
                registerUser.setText(stringBuilder)
                registerUser.setSelection(stringBuilder.length)
            } else {
                count = registerUser.length()
            }
        }

        private fun registerPwd() = mBinding.apply {
            if (registerPwd.length() > count) {
                val stringBuilder = StringBuilder()
                registerPwd.text.forEach {
                    if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
                        stringBuilder.append(it)
                    }
                }
                count = stringBuilder.length
                registerPwd.setText(stringBuilder)
                registerPwd.setSelection(stringBuilder.length)
            } else {
                count = registerPwd.length()
            }
        }

        private fun registerPwdCheck() = mBinding.apply {
            if (registerPwdCheck.length() > count) {
                val stringBuilder = StringBuilder()
                registerPwdCheck.text.forEach {
                    if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
                        stringBuilder.append(it)
                    }
                }
                count = stringBuilder.length
                registerPwdCheck.setText(stringBuilder)
                registerPwdCheck.setSelection(stringBuilder.length)
            } else {
                count = registerPwdCheck.length()
            }
        }

        private fun registerBtn() = mBinding.apply {
            val judgeResult = judgeText()
            if (judgeResult[1] == false)
                root.showSnackBar("${judgeResult[0]}")
            else {
                registerJudge()
            }
        }

        private fun judgeText(): ArrayList<Any> {
            mBinding.apply {
                return if (registerUser.length() < 6 || registerUser.length() > 16) {
                    arrayListOf("设置的账号不符合规范！", false)
                } else if (registerPwd.length() < 6 || registerUser.length() > 16) {
                    arrayListOf("设置的密码不符合规范！", false)
                } else if (registerPwdCheck.length() < 6 || registerPwdCheck.length() > 16) {
                    arrayListOf("二次确认所输入的密码不符合规范！", false)
                } else if (registerPwd.text.toString() != registerPwdCheck.text.toString()) {
                    registerPwdCheck.text = null
                    arrayListOf("两次输入的密码不匹配", false)
                } else if (registerId.length() < 1) {
                    arrayListOf("设置的Uid不符合规范！", false)
                } else {
                    arrayListOf("", true)
                }
            }
        }

        private fun registerJudge() = mBinding.apply {
            reloading()
            val signUpTask =
                FutureTask(
                    NetworkThread(
                        registerUser.text.toString(),
                        registerPwd.text.toString(),
                        registerId.text.toString(),
                        url = NetworkSettings.SIGN_UP
                    )
                )
            Thread(signUpTask).start()
            try {
                thread {
                    val body = Gson().fromJson(signUpTask.get(), ResponseBody::class.java)
                    runOnUiThread {
                        if (body.code == 200) {
                            mBinding.root.showSnackBar("注册成功！")
                        } else if (body.code == 500) {
                            mBinding.root.showSnackBar("注册失败")
                        } else {
                            mBinding.root.showSnackBar("账号已存在，请换一个试试。")
                        }
                        unloading()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                unloading()
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

    private val mBinding by lazy { RegisterActivityBinding.inflate(layoutInflater) }
    private val main = this
    private var eventHandler = EventHandler()
    private val registerManager = RegisterManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        registerManager.initUI()
        registerManager.initComponent()
    }

    override fun onDestroy() {
        super.onDestroy()
        SMSSDK.unregisterAllEventHandler()
        SMSSDK.unregisterEventHandler(eventHandler)
    }

}