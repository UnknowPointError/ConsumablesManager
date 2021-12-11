package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.CompoundButton
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.ActivityMainBinding
import cn.example.consumablesManagement.databinding.LoginActivityBinding
import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.*
import cn.example.consumablesManagement.util.SpUtil.getBoolean
import cn.example.consumablesManagement.util.SpUtil.getString
import cn.example.consumablesManagement.util.SpUtil.putBoolean
import cn.example.consumablesManagement.util.SpUtil.putString
import cn.example.consumablesManagement.util.SpUtil.removeValue
import cn.example.consumablesManagement.util.ShowUtil.showSnackBar
import cn.example.consumablesManagement.view.adapter.LoginRecyclerViewAdapter
import com.google.gson.Gson
import java.lang.StringBuilder
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread

class LoginActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility", "InflateParams")
    inner class LoginManager(private val mBinding: LoginActivityBinding) {

        private val data = ArrayList<LoginRecyclerViewAdapter.User>()
        private val aes = AES()

        fun initUI() {
            supportActionBar?.hide()
            window.statusBarColor = Color.TRANSPARENT
            mBinding.apply {
                loginPwd.inputType = 0x00000081
                if (getString("username") != "")
                    loginRecyclerView.visibility = View.VISIBLE
                else // 如果有保存的账号，则显示下拉列表按钮 否则不显示下拉列表按钮
                    loginRecyclerView.visibility = View.GONE
                // 读取true＆false 初始化复选框勾选状态
                if (getBoolean("RemeberUser") as Boolean) {
                    loginRemeberUser.isChecked = true
                    if (getBoolean("RemeberPwd") == true) {
                        loginRemeberPwd.isChecked = true
                        loginAutoRemember.isChecked = getBoolean("RemeberAutoLogin") == true
                    }
                }
                if (loginAutoRemember.isChecked) {
                    val lastLoginUser = getString("LastLoginUser")
                    if (lastLoginUser != "") {
                        loginUser.setText(lastLoginUser)
                        loginPwd.setText(
                            aes.decrypt(
                                getString("password$lastLoginUser"),
                                aes.loadKeyAES(viewModel.aesKEY.value)
                            )
                        )
                        if (intent.getBooleanExtra("ExitByUser", true))
                            login()
                    }
                }
            }
        }

        fun initComponent() = mBinding.apply {
            loginUser.addTextChangedListener { if (viewModel.working.value == false) initLoginUser() }
            loginPwd.addTextChangedListener { if (viewModel.working.value == false) initLoginPwd("textChanged") }
            loginRemeberPwd.setOnCheckedChangeListener { buttonView, _ ->
                reinitializeCheck(
                    buttonView
                )
            }
            loginClearPwdImage.setOnClickListener { reinitializeClearPwd("pwd") }
            loginClearUserImage.setOnClickListener { reinitializeClearPwd("user") }
            loginRemeberUser.setOnCheckedChangeListener { buttonView, _ ->
                reinitializeCheck(
                    buttonView
                )
            }
            loginRegisterTextView.setOnClickListener { StartUtil.startActivity<RegisterActivity> { } }
            loginRecyclerView.setOnClickListener { initRecyclerView() }
            loginPwdImage.setOnTouchListener { _, event ->
                if (viewModel.working.value == false)
                    initLoginPwd("onTouch", event)
                true
            }
            loginBtn.setOnClickListener { login() }
            loginAutoRemember.setOnCheckedChangeListener { buttonView, _ ->
                reinitializeCheck(
                    buttonView
                )
            }
        }

        private fun login() = mBinding.apply {
            loading.reloading(context = this@LoginActivity)
            val userText = loginUser.text.toString()
            val pwdText = loginPwd.text.toString()
            TryCatchUtil.tryCatch({
                thread {
                    val body = NetWork.connect<ResponseBody>(
                        NetworkThread(
                            userText,
                            pwdText,
                            url = NetworkSettings.SIGN_IN
                        )
                    )
                    runOnUiThread {
                        if (body.code == 200) {
                            if (loginRemeberUser.isChecked) {
                                val username = getString("username")
                                if (username == "") putString(
                                    "username",
                                    "${loginUser.text}"
                                )
                                else if (username?.contains(loginUser.text) == false) putString(
                                    "username",
                                    "$username,${loginUser.text}"
                                )
                                loginRecyclerView.visibility = View.VISIBLE
                            } else {
                                getString("username").apply {
                                    val userContent = loginUser.text
                                    when {
                                        this?.contains(",$userContent") == true -> {
                                            removeValue("username")
                                            putString(
                                                "username",
                                                this.removeSuffix(",$userContent")
                                            )
                                        }
                                        this?.contains("$userContent,") == true -> {
                                            removeValue("username")
                                            putString(
                                                "username",
                                                this.removePrefix("$userContent,")
                                            )
                                        }
                                        this?.contains("$userContent") == true -> {
                                            removeValue("username")
                                            putString(
                                                "username",
                                                this.removePrefix(userContent)
                                            )
                                            loginRecyclerView.visibility = View.GONE
                                        }
                                    }
                                }
                            }
                            if (loginRemeberPwd.isChecked) {
                                if (getString("password${loginUser.text}") == "")
                                    putString(
                                        "password${loginUser.text}",
                                        aes.encrypt(
                                            loginPwd.text.toString(),
                                            aes.loadKeyAES(viewModel.aesKEY.value)
                                        )
                                    )
                                loginRecyclerView.visibility = View.VISIBLE
                            } else removeValue("password${loginUser.text}")
                            mBinding.root.showSnackBar("登录成功！")
                            USERDATA.userName = loginUser.text.toString()
                            putString(
                                "LastLoginUser",
                                mBinding.loginUser.text.toString()
                            )
                            finish()
                            StartUtil.startActivity<HomeActivity> { }
                        } else mBinding.root.showSnackBar("登录失败！请检查账号密码是否正确！")
                        loading.unloading()
                    }

                }
            }, {
                loading.unloading()
            }, {
                thread {
                    Thread.sleep(4000)
                    if (loading.isloading() == true) {
                        loading.unloading()
                        runOnUiThread { root.showSnackBar("网络异常。") }
                    }
                }
            })
        }

        private fun reinitializeClearPwd(name: String) = mBinding.apply {
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

        private fun reinitializeCheck(checkBox: CompoundButton) = mBinding.apply {
            when (checkBox.id) {
                loginRemeberUser.id -> {
                    val isRemeberUser = getBoolean("RemeberUser")
                    if (loginRemeberUser.isChecked) {
                        if (isRemeberUser == false)
                            putBoolean("RemeberUser", true)
                    } else {
                        if (isRemeberUser == true)
                            putBoolean("RemeberUser", false)
                        loginRemeberPwd.isChecked = false
                        loginAutoRemember.isChecked = false
                    }
                }
                loginRemeberPwd.id -> {
                    val isRemeberPwd = getBoolean("RemeberPwd")
                    if (loginRemeberPwd.isChecked) {
                        loginRemeberUser.isChecked = true
                        if (isRemeberPwd == false)
                            putBoolean("RemeberPwd", true)
                    } else {
                        if (isRemeberPwd == true)
                            putBoolean("RemeberPwd", false)
                        loginAutoRemember.isChecked = false
                    }
                }
                loginAutoRemember.id -> {
                    val autoLogin = getBoolean("RemeberAutoLogin")
                    if (loginAutoRemember.isChecked) {
                        loginRemeberUser.isChecked = true
                        loginRemeberPwd.isChecked = true
                        if (autoLogin == false)
                            putBoolean("RemeberAutoLogin", true)
                    } else {
                        if (autoLogin == true)
                            putBoolean("RemeberAutoLogin", false)
                    }
                }
            }
        }

        private fun initRecyclerView() = mBinding.apply {
            data.clear()
            viewModel.userSplitContent.value?.clear()
            val userContent = getString("username")!!
            if (userContent.contains(",")) (viewModel.userSplitContent.value)?.plusAssign(
                userContent.split(",").toMutableList()
            )
            else viewModel.userSplitContent.value?.add(userContent)
            val size = viewModel.userSplitContent.value?.size
            repeat(size!!) {
                data.add(
                    LoginRecyclerViewAdapter.User(
                        viewModel.userSplitContent.value?.get(
                            it
                        )!!
                    )
                )
            }
            val popupView = layoutInflater.inflate(R.layout.login_recyclerview, null)
            val popupWindow = PopupWindow(popupView, longinUserCardView.width, 200)
            val loginListPop = popupView.findViewById(R.id.loginRecyclerView) as RecyclerView
            loginListPop.layoutManager = LinearLayoutManager(this@LoginActivity)
            val adapter = LoginRecyclerViewAdapter(data)
            adapter.nameBlock = {
                val key = aes.loadKeyAES(viewModel.aesKEY.value)
                getString("password${it.userName}").apply {
                    loginPwd.text = null
                    loginUser.setText(it.userName)
                    loginUser.setSelection(loginUser.length())
                    if (!this.isNullOrBlank())
                        loginPwd.setText(aes.decrypt(this, key))
                }
                popupWindow.dismiss()
            }
            adapter.cancleBlock = {
                AlertDialog.Builder(this@LoginActivity).apply {
                    setTitle("提示")
                    setMessage("你确定删除当前选择的账号吗？")
                    setIcon(R.drawable.tips)
                    setCancelable(true)
                    setPositiveButton("确定") { dialog, _ ->
                        val userData = getString("username")!!
                        removeValue("username")
                        removeValue("password${it.userName}")
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
                    viewModel.isWorking()
                    if (loginPwd.length() > 0) {
                        val stringBuilder = StringBuilder()
                        loginPwd.text.forEach {
                            if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z')) {
                                stringBuilder.append(it)
                            }
                        }
                        loginPwd.setText(stringBuilder)
                        loginPwd.setSelection(loginPwd.length())
                    }
                    if (loginClearPwdImage.visibility == View.INVISIBLE)
                        loginClearPwdImage.visibility = View.VISIBLE
                    if (loginPwdImage.visibility == View.INVISIBLE)
                        loginPwdImage.visibility = View.VISIBLE
                    if (loginPwd.text.isEmpty()) {
                        loginPwdImage.visibility = View.INVISIBLE
                        loginClearPwdImage.visibility = View.INVISIBLE
                    }
                    viewModel.cancelWork()
                }
                "onTouch" -> {
                    when (event?.action) {
                        KeyEvent.ACTION_UP -> {
                            loginPwd.inputType = 0x00000081
                            loginPwd.setSelection(loginPwd.length())
                            loginPwdImage.setImageResource(R.drawable.eye)
                        }
                        KeyEvent.ACTION_DOWN -> {
                            loginPwd.inputType = 0x00000001
                            loginPwd.setSelection(loginPwd.length())
                            loginPwdImage.setImageResource(R.drawable.eye_slash)
                        }
                    }
                }
            }
        }

        private fun initLoginUser() = mBinding.apply {
            viewModel.isWorking()
            if (loginUser.length() > 0) {
                val stringBuilder = StringBuilder()
                loginUser.text.forEach {
                    if ((it in '0'..'9') || (it in 'a'..'z') || (it in 'A'..'Z') || (it == '@' || it == '.')) {
                        stringBuilder.append(it)
                    }
                }
                loginUser.setText(stringBuilder)
                loginUser.setSelection(loginUser.length())
            }
            if (loginClearUserImage.visibility == View.INVISIBLE)
                loginClearUserImage.visibility = View.VISIBLE
            if (loginUser.text.isEmpty())
                loginClearUserImage.visibility = View.INVISIBLE
            viewModel.cancelWork()
        }

    }

    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val loginManager by lazy { LoginManager(mBinding.loginLayout) }
    private val loading = Loading()
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        loginManager.initUI()
        loginManager.initComponent()
    }

}