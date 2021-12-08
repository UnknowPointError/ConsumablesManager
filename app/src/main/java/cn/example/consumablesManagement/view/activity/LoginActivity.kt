package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.ActivityMainBinding
import cn.example.consumablesManagement.databinding.LoginActivityBinding
import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.AES
import cn.example.consumablesManagement.util.AES.decrypt
import cn.example.consumablesManagement.util.AES.encrypt
import cn.example.consumablesManagement.util.Loading.reloading
import cn.example.consumablesManagement.util.Loading.unloading
import cn.example.consumablesManagement.util.TRYCATCH
import cn.example.consumablesManagement.util.Toasts.showSnackBar
import cn.example.consumablesManagement.util.USERDATA
import cn.example.consumablesManagement.view.adapter.LoginRecyclerViewAdapter
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.StringBuilder
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    inner class LoginManager(private val mBinding: LoginActivityBinding) {

        private val data = ArrayList<LoginRecyclerViewAdapter.User>()
        private var count = 0
        private val aesKey = "PASSWORDAAAAAAAAAAAAAA=="
        private val sharedPreferences = getSharedPreferences("user", Context.MODE_PRIVATE)
        private var userSplitContent: MutableList<String> = mutableListOf()

        fun initUI() {
            supportActionBar?.hide()
            window.statusBarColor = Color.TRANSPARENT
            mBinding.apply {
                loginPwd.inputType = 0x00000081
                sharedPreferences.apply {
                    if (getString("username", "") != "")
                        loginRecyclerView.visibility = View.VISIBLE
                    else // 如果有保存的账号，则显示下拉列表按钮 否则不显示下拉列表按钮
                        loginRecyclerView.visibility = View.GONE
                    // 读取true＆false 初始化复选框勾选状态
                    if (getBoolean("RemeberUser", false)) {
                        loginRemeberUser.isChecked = true
                        if (getBoolean("RemeberPwd", false)) {
                            loginRemeberPwd.isChecked = true
                            loginAutoRemember.isChecked = getBoolean("RemeberAutoLogin", false)
                        }
                    }
                    if (loginAutoRemember.isChecked) {
                        val lastLoginUser = getString("LastLoginUser", "")
                        if (lastLoginUser != "") {
                            loginUser.setText(lastLoginUser)
                            loginPwd.setText(
                                getString(
                                    "password$lastLoginUser",
                                    ""
                                )?.decrypt(AES.loadKeyAES(aesKey))
                            )
                            if (intent.getBooleanExtra("ExitByUser", true))
                                login()
                        }
                    }
                }
            }
        }

        fun initComponent() = mBinding.apply {
            loginUser.addTextChangedListener { initLoginUser() }
            loginPwd.addTextChangedListener { initLoginPwd("textChanged") }
            loginRemeberPwd.setOnCheckedChangeListener { buttonView, _ -> initChecked(buttonView) }
            loginClearPwdImage.setOnClickListener { initClear("pwd") }
            loginClearUserImage.setOnClickListener { initClear("user") }
            loginRemeberUser.setOnCheckedChangeListener { buttonView, _ -> initChecked(buttonView) }
            loginRegisterTextView.setOnClickListener { startActivity<RegisterActivity> { } }
            loginRecyclerView.setOnClickListener { initRecyclerView() }
            loginPwdImage.setOnTouchListener { _, event ->
                initLoginPwd("onTouch", event)
                true
            }
            loginBtn.setOnClickListener { login() }
            loginAutoRemember.setOnCheckedChangeListener { buttonView, _ -> initChecked(buttonView) }
        }

        private fun login() = mBinding.apply {
            reloading()
            val userText = loginUser.text.toString()
            val pwdText = loginPwd.text.toString()
            val task = FutureTask(NetworkThread(userText, pwdText, url = NetworkSettings.SIGN_IN))
            Thread(task).start()
            TRYCATCH.tryFunc({
                thread {
                    runBlocking {
                        val coroutine = launch {
                            delay(4000)
                            unloading()
                            runOnUiThread { root.showSnackBar("网络异常。") }
                        }
                        launch {
                            thread {
                                val body = Gson().fromJson(task.get(), ResponseBody::class.java)
                                runOnUiThread {
                                    if (body.code == 200) {
                                        if (loginRemeberUser.isChecked) saveUserData()
                                        else clearUserData()
                                        if (loginRemeberPwd.isChecked) savePwdData()
                                        else clearPwdData()
                                        mBinding.root.showSnackBar("登录成功！")
                                        USERDATA.userName = loginUser.text.toString()
                                        saveLastLoginUser()
                                        finish()
                                        startActivity<HomeActivity> { }
                                    } else mBinding.root.showSnackBar("登录失败！请检查账号密码是否正确！")
                                    unloading()
                                    coroutine.cancel()
                                }
                            }
                        }
                    }
                }
            }, {
                unloading()
            })
        }

        private fun saveUserData() = mBinding.apply {
            getSharedPreferences("user", Context.MODE_PRIVATE).apply {
                val username = getString("username", "")
                if (username == "") {
                    edit().apply {
                        putString("username", "${loginUser.text}")
                        apply()
                    }
                } else {
                    if (username?.contains(loginUser.text) == false) {
                        edit().apply {
                            putString("username", "$username,${loginUser.text}")
                            apply()
                        }
                    }
                }
            }
            loginRecyclerView.visibility = View.VISIBLE
        }

        private fun savePwdData() = mBinding.apply {
            val key = AES.loadKeyAES(aesKey)
            getSharedPreferences("user", Context.MODE_PRIVATE).apply {
                val password = getString("password${loginUser.text}", "")
                if (password == "") {
                    edit().apply {
                        putString(
                            "password${loginUser.text}",
                            loginPwd.text.toString().encrypt(key)
                        )
                        apply()
                    }
                }
            }
            loginRecyclerView.visibility = View.VISIBLE
        }

        private fun saveLastLoginUser() {
            sharedPreferences.edit()
                .putString("LastLoginUser", mBinding.loginUser.text.toString())
                .apply()
        }

        private fun clearPwdData() = mBinding.apply {
            sharedPreferences.edit()
                .remove("password${loginUser.text}")
                .apply()
        }

        private fun clearUserData() = mBinding.apply {
            sharedPreferences.apply {
                getString("username", "")?.let {
                    val userText = loginUser.text
                    if (it.contains(",$userText"))
                        edit()
                            .remove("username")
                            .putString("username", it.removeSuffix(",$userText"))
                            .apply()
                    else if (it.contains("$userText,"))
                        edit()
                            .remove("username")
                            .putString("username", it.removePrefix("$userText,"))
                            .apply()
                    else if (it.contains("$userText")) {
                        edit()
                            .remove("username")
                            .putString("username", it.removePrefix(userText))
                            .apply()
                        loginRecyclerView.visibility = View.GONE
                    }

                }
            }
        }

        private fun initClear(name: String) = mBinding.apply {
            when (name) {
                "user" -> {
                    loginUser.text = null
                    loginClearUserImage.visibility = View.INVISIBLE
                }
                "pwd" -> {
                    loginPwd.text = null
                    loginClearPwdImage.visibility = View.INVISIBLE
                    loginPwdImage.visibility = View.INVISIBLE
                }
            }
        }

        private fun initChecked(checkBox: CompoundButton) = mBinding.apply {
            when (checkBox.id) {
                loginRemeberUser.id -> sharedPreferences.apply {
                    val isRemeberUser = getBoolean("RemeberUser", false)
                    if (loginRemeberUser.isChecked) {
                        if (!isRemeberUser) {
                            edit().apply {
                                putBoolean("RemeberUser", true)
                                apply()
                            }
                        }
                    } else {
                        if (isRemeberUser) {
                            edit().apply {
                                putBoolean("RemeberUser", false)
                                apply()
                            }
                        }
                        loginRemeberPwd.isChecked = false
                        loginAutoRemember.isChecked = false
                    }
                }
                loginRemeberPwd.id -> sharedPreferences.apply {
                    val isRemeberPwd = getBoolean("RemeberPwd", false)
                    if (loginRemeberPwd.isChecked) {
                        loginRemeberUser.isChecked = true
                        if (!isRemeberPwd) {
                            edit().apply {
                                putBoolean("RemeberPwd", true)
                                apply()
                            }
                        }
                    } else {
                        if (isRemeberPwd) {
                            edit().apply {
                                putBoolean("RemeberPwd", false)
                                apply()
                            }
                        }
                        loginAutoRemember.isChecked = false
                    }
                }
                loginAutoRemember.id -> sharedPreferences.apply {
                    val autoLogin = getBoolean("RemeberAutoLogin", false)
                    if (loginAutoRemember.isChecked) {
                        loginRemeberUser.isChecked = true
                        loginRemeberPwd.isChecked = true
                        if (!autoLogin)
                            edit().apply {
                                putBoolean("RemeberAutoLogin", true)
                                apply()
                            }
                    } else {
                        if (autoLogin)
                            edit().apply {
                                putBoolean("RemeberAutoLogin", false)
                                apply()
                            }
                    }
                }
            }
        }

        private fun initRecyclerView() = mBinding.apply {
            data.clear()
            userSplitContent.clear()
            val userContent = sharedPreferences.getString("username", "")
            userContent?.let {
                if (it.contains(","))
                    userSplitContent = it.split(",").toMutableList()
                else
                    userSplitContent.add(it)
            }
            val size = userSplitContent.size
            repeat(size) {
                data.add(LoginRecyclerViewAdapter.User(userSplitContent[it]))
            }
            val popupView = layoutInflater.inflate(R.layout.login_recyclerview, null)
            val popupWindow = PopupWindow(popupView, longinUserCardView.width, 200)
            val loginListPop = popupView.findViewById(R.id.loginRecyclerView) as RecyclerView
            loginListPop.layoutManager = LinearLayoutManager(main)
            val adapter = LoginRecyclerViewAdapter(data)
            adapter.nameBlock = {
                val key = AES.loadKeyAES(aesKey)
                sharedPreferences.getString("password${it.userName}", "").apply {
                    loginPwd.text = null
                    loginUser.setText(it.userName)
                    loginUser.setSelection(loginUser.length())
                    if (!this.isNullOrBlank())
                        loginPwd.setText(this.decrypt(key))

                }
                popupWindow.dismiss()
            }
            adapter.cancleBlock = {
                AlertDialog.Builder(main).apply {
                    setTitle("提示")
                    setMessage("你确定删除当前选择的账号吗？")
                    setIcon(R.drawable.tips)
                    setCancelable(true)
                    setPositiveButton("确定") { dialog, _ ->
                        sharedPreferences.apply {
                            val userData = getString("username", "").toString()
                            edit().apply {
                                remove("username")
                                remove("password${it.userName}")
                                when {
                                    userData.contains(",${it.userName}") -> putString(
                                        "username",
                                        userData.removeSuffix(",${it.userName}")
                                    )
                                    userData.contains("${it.userName},") -> putString(
                                        "username",
                                        userData.removePrefix("${it.userName},")
                                    )
                                    else -> loginRecyclerView.visibility = View.GONE
                                }
                                apply()
                            }
                        }
                        popupWindow.dismiss()
                        dialog.dismiss()
                    }
                    setNegativeButton("取消") { dialog, _ ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
            loginListPop.adapter = adapter
            loginListPop.background.alpha = 255
            popupWindow.isOutsideTouchable = true
            popupWindow.showAsDropDown(longinUserCardView, 0, 5)
        }

        private fun initLoginPwd(mode: String, event: MotionEvent? = null) = mBinding.apply {
            when (mode) {
                "textChanged" -> {
                    if (loginPwd.length() > count) {
                        val stringBuilder = StringBuilder()
                        loginPwd.text.forEach {
                            if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
                                stringBuilder.append(it)
                            }
                        }
                        count = stringBuilder.length
                        loginPwd.setText(stringBuilder)
                        loginPwd.setSelection(loginPwd.length())
                    } else {
                        count = loginPwd.length()
                    }
                    if (loginClearPwdImage.visibility == View.INVISIBLE)
                        loginClearPwdImage.visibility = View.VISIBLE
                    if (loginPwdImage.visibility == View.INVISIBLE)
                        loginPwdImage.visibility = View.VISIBLE
                    if (loginPwd.text.isEmpty()) {
                        loginPwdImage.visibility = View.INVISIBLE
                        loginClearPwdImage.visibility = View.INVISIBLE
                    }
                }
                "onTouch" -> {
                    when (event?.action) {
                        KeyEvent.ACTION_UP -> {
                            loginPwd.inputType = 0x00000081
                            loginPwd.setSelection(loginPwd.length())
                            loginPwdImage.setImageResource(R.drawable.eye_1)
                        }
                        KeyEvent.ACTION_DOWN -> {
                            loginPwd.inputType = 0x00000001
                            loginPwd.setSelection(loginPwd.length())
                            loginPwdImage.setImageResource(R.drawable.eye_slash_1)
                        }
                    }
                }
            }
        }

        private fun initLoginUser() = mBinding.apply {
            if (loginUser.length() > count) {
                val stringBuilder = StringBuilder()
                loginUser.text.forEach {
                    if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z') || (it == '@' || it == '.')) {
                        stringBuilder.append(it)
                    }
                }
                count = stringBuilder.length
                loginUser.setText(stringBuilder)
                loginUser.setSelection(loginUser.length())
            } else {
                count = loginUser.length()
            }
            if (loginClearUserImage.visibility == View.INVISIBLE)
                loginClearUserImage.visibility = View.VISIBLE
            if (loginUser.text.isEmpty()) {
                loginClearUserImage.visibility = View.INVISIBLE
            }
        }

        private inline fun <reified T> startActivity(block: Intent.() -> Unit) {
            val intent = Intent(main, T::class.java)
            intent.block()
            main.startActivity(intent)
        }
    }

    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val loginManager by lazy { LoginManager(mBinding.loginLayout) }
    private val main = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        loginManager.initUI()
        loginManager.initComponent()
    }

}