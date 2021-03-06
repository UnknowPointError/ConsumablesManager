package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.ActivityMainBinding
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.*
import cn.example.consumablesManagement.util.appUtil.SPUtil.getBoolean
import cn.example.consumablesManagement.util.appUtil.SPUtil.getString
import cn.example.consumablesManagement.util.appUtil.SPUtil.putBoolean
import cn.example.consumablesManagement.util.appUtil.SPUtil.putString
import cn.example.consumablesManagement.util.appUtil.SPUtil.removeValue
import cn.example.consumablesManagement.util.ktUtil.AES
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showSnackBar
import cn.example.consumablesManagement.util.appUtil.StartActivityUtil.startActivity
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.networkInterval
import cn.example.consumablesManagement.view.activity.viewModel.LoginViewModel
import cn.example.consumablesManagement.view.adapter.LoginRecyclerViewAdapter
import android.widget.EditText


// @formatter:off
@SuppressLint("ClickableViewAccessibility")
class LoginActivity : AppCompatActivity() {

    private val aes = AES()
    private val mBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val loginLayout by lazy { mBinding.loginLayout }
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initObserver()
        initialize()
    }

    // [2021/12/30 9:42] ????????????????????????????????????
    override fun dispatchTouchEvent(event:MotionEvent): Boolean{
        if (event.action == MotionEvent.ACTION_DOWN){
            val viewFocus = currentFocus
            if (viewFocus is EditText){
                val outRect = Rect()
                viewFocus.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())){
                    viewFocus.clearFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(viewFocus.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    // [2021/12/30 9:43] ??????????????????
    private fun initObserver() = loginLayout.apply {
        viewModel = ViewModelProvider(this@LoginActivity).get(LoginViewModel::class.java)
        viewModel.responseBody.observe(this@LoginActivity) {
            if (it.code == 200) {
                loginLayout.root.showSnackBar(it.data.toString())
                if (loginRemeberUser.isChecked) viewModel.toSaveUser(loginUser.loginEditText.text.toString())
                else viewModel.toRemoveUser(loginUser.loginEditText.text.toString())
                if (loginRemeberPwd.isChecked) viewModel.toSavePwd(loginUser.loginEditText.text.toString(),aes.encrypt(loginPwd.loginEditText.text.toString(), aes.loadKeyAES(AppData.AES_KEY)))
                else viewModel.toRemovePwd(loginUser.loginEditText.text.toString())
                AppData.userName = loginUser.loginEditText.text.toString()
                putString(viewModel.lastLoginUser, loginUser.loginEditText.text.toString())
                startActivity<HomeActivity> { }
                finish()
            } else {
                loginLayout.root.showSnackBar(it.data.toString())
            }
        }
        viewModel.drowDownList.observe(this@LoginActivity) {
            if (it) loginUser.loginEndImage.visibility = View.GONE
            else loginUser.loginEndImage.visibility = View.VISIBLE
        }
        viewModel.userContent.observe(this@LoginActivity) {
            loginUser.loginEditText.setText(it)
            loginUser.loginEditText.setSelection(loginUser.loginEditText.length())
            if (loginUser.loginEditText.length() != 0 && loginUser.loginEditText.isFocused)
                loginUser.loginStartImage.visibility = View.VISIBLE
            else
                loginUser.loginStartImage.visibility = View.INVISIBLE
        }
        viewModel.pwdContent.observe(this@LoginActivity) {
            loginPwd.loginEditText.setText(it)
            loginPwd.loginEditText.setSelection(loginPwd.loginEditText.length())
            if (loginPwd.loginEditText.length() != 0 && loginPwd.loginEditText.isFocused)  {
                loginPwd.loginStartImage.visibility = View.VISIBLE
                loginPwd.loginEndImage.visibility = View.VISIBLE
            } else {
                loginPwd.loginEndImage.visibility = View.INVISIBLE
                loginPwd.loginEndImage.visibility = View.INVISIBLE
            }
        }
        viewModel.newUserName.observe(this@LoginActivity) {
            loginUser.loginEditText.setText(it)
        }
        viewModel.newPassWord.observe(this@LoginActivity) {
            loginPwd.loginEditText.setText(it)
        }
    }

    // [2021/12/12 22:49] ?????????UI
    private fun initialize() = loginLayout.apply {
        initLoadView { user, pwd, autoLogin ->
            if(user) loginRemeberUser.isChecked = true
            if(pwd) loginRemeberPwd.isChecked = true
            if(autoLogin) loginAutoRemember.isChecked = true
            if (isNotBlank()) loginUser.loginEndImage.visibility = View.VISIBLE
            else loginUser.loginEndImage.visibility = View.GONE
        }
        supportActionBar?.hide()
        window.statusBarColor = Color.TRANSPARENT
        loginPwd.loginEditText.inputType = AppData.INPUTTEXT_PASSWORD
        loginUser.loginStartImage.setImageResource(R.drawable.close)
        loginUser.loginEndImage.setImageResource(R.drawable.drop_downlist)
        loginPwd.loginStartImage.setImageResource(R.drawable.close)
        loginPwd.loginEndImage.setImageResource(R.drawable.eye_slash)
        loginUser.loginEditText.addTextChangedListener { actionInterval{ viewModel.setUserEditTextStatus(loginUser.loginEditText.text!!) } }
        loginPwd.loginEditText.addTextChangedListener { actionInterval { viewModel.setPwdEditTextStatus(loginPwd.loginEditText.text!!) } }
        loginRegisterTextView.setOnClickListener { actionInterval { startActivity<RegisterActivity> { } } }
        loginUser.loginEndImage.setOnClickListener {
            actionInterval {
                if (!viewModel.dropDownListStatus) {
                    viewModel.dropDownListStatus = true
                    loginUser.loginEndImage.setImageResource(R.drawable.drop_downclose)
                    setDropDownList()
                } else {
                    viewModel.dropDownListStatus = false
                    loginUser.loginEndImage.setImageResource(R.drawable.drop_downlist)
                }
            }
        }
        loginUser.loginEditText.setOnFocusChangeListener { _, hasFocus ->
            actionInterval {
                if (hasFocus && loginUser.loginEditText.length() != 0) loginUser.loginStartImage.visibility = View.VISIBLE
                else loginUser.loginStartImage.visibility = View.INVISIBLE
            }
        }
        loginPwd.loginEditText.setOnFocusChangeListener { _, hasFocus ->
            actionInterval {
                if (hasFocus && loginPwd.loginEditText.length() != 0) {
                    loginPwd.loginStartImage.visibility = View.VISIBLE
                    loginPwd.loginEndImage.visibility = View.VISIBLE
                } else {
                    loginPwd.loginEndImage.visibility = View.INVISIBLE
                    loginPwd.loginStartImage.visibility = View.INVISIBLE
                }
            }
        }
        loginBtn.setOnClickListener { networkInterval { login() } }
        loginPwd.loginStartImage.setOnClickListener {
            actionInterval {
                loginPwd.loginEditText.text = null
                loginPwd.loginStartImage.visibility = View.INVISIBLE
                loginPwd.loginEndImage.visibility = View.INVISIBLE
            }
        }
        loginUser.loginStartImage.setOnClickListener {
            actionInterval {
                loginUser.loginEditText.text = null
                loginUser.loginStartImage.visibility = View.INVISIBLE
            }
        }
        loginPwd.loginEndImage.setOnTouchListener{ _, event ->
            actionInterval {
                if(event?.action == KeyEvent.ACTION_UP) {
                    if (loginPwd.loginEditText.inputType == AppData.INPUTTEXT_NORMAL) {
                        loginPwd.loginEditText.inputType = AppData.INPUTTEXT_PASSWORD
                        loginPwd.loginEndImage.setImageResource(R.drawable.eye_slash)
                    } else {
                        loginPwd.loginEditText.inputType = AppData.INPUTTEXT_NORMAL
                        loginPwd.loginEndImage.setImageResource(R.drawable.eye)
                    }
                    loginPwd.loginEditText.setSelection(loginPwd.loginEditText.length())
                }
            }
            true
        }
        loginRemeberUser.setOnCheckedChangeListener { _, _ ->
            actionInterval {
                if (loginRemeberUser.isChecked) {
                    if (!getBoolean(viewModel.remeberUser)) putBoolean(viewModel.remeberUser, true)
                } else {
                    if (getBoolean(viewModel.remeberUser)) putBoolean(viewModel.remeberUser, false)
                    if (getBoolean(viewModel.remeberPwd)) putBoolean(viewModel.remeberPwd, false)
                    if (getBoolean(viewModel.remeberLogin)) putBoolean(viewModel.remeberLogin, false)
                    loginRemeberPwd.isChecked = false
                    loginAutoRemember.isChecked = false
                }
            }
        }
        loginRemeberPwd.setOnCheckedChangeListener { _, _ ->
            actionInterval{
                if (loginRemeberPwd.isChecked) {
                    loginRemeberUser.isChecked = true
                    if (!getBoolean(viewModel.remeberUser)) putBoolean(viewModel.remeberUser, true)
                    if (!getBoolean(viewModel.remeberPwd)) putBoolean(viewModel.remeberPwd, true)
                } else {
                    if (getBoolean(viewModel.remeberPwd)) putBoolean(viewModel.remeberPwd, false)
                    if (getBoolean(viewModel.remeberLogin)) putBoolean(viewModel.remeberLogin, false)
                    loginRemeberPwd.isChecked = false
                    loginAutoRemember.isChecked = false
                }
            }
        }
        loginAutoRemember.setOnCheckedChangeListener { _, _ ->
            actionInterval{
                if (loginAutoRemember.isChecked) {
                    if (!getBoolean(viewModel.remeberLogin)) putBoolean(viewModel.remeberLogin, true)
                    if (!getBoolean(viewModel.remeberUser)) putBoolean(viewModel.remeberUser, true)
                    if (!getBoolean(viewModel.remeberPwd)) putBoolean(viewModel.remeberPwd, true)
                    loginRemeberUser.isChecked = true
                    loginRemeberPwd.isChecked = true
                } else {
                    if (getBoolean(viewModel.remeberLogin)) putBoolean(viewModel.remeberLogin, false)
                }
            }
        }
    }

    // [2021/12/30 9:40] ???????????????????????????
    private inline fun initLoadView(block : String.(user: Boolean,pwd : Boolean, autoLogin: Boolean) -> Unit) = loginLayout.apply {
        val userName = getString(viewModel.userName)
        val isRemeberUser = getBoolean(viewModel.remeberUser)
        val isRemeberPwd = getBoolean(viewModel.remeberPwd)
        val isRemeberLogin = getBoolean(viewModel.remeberLogin)
        if (viewModel.getSpData(userName, aes, isRemeberUser, isRemeberPwd) && isRemeberLogin) {
            userName.block(isRemeberUser,isRemeberPwd,isRemeberLogin)
            if (intent.getBooleanExtra(AppData.EXIT_BY_USER,true)) {
                login(viewModel.newUserName.value!!,viewModel.newPassWord.value!!)
                supportActionBar?.hide()
                window.statusBarColor = Color.TRANSPARENT
                return@apply
            }
        } else { userName.block(isRemeberUser,isRemeberPwd,isRemeberLogin) }
    }

    // [2021/12/13 14:11] ??????
    private fun login(user : String = loginLayout.loginUser.loginEditText.text.toString(), pwd : String = loginLayout.loginPwd.loginEditText.text.toString()) {
        loginLayout.apply {
            viewModel.setResponseData(this@LoginActivity,NetworkThread(user, pwd, url = AppData.SIGN_IN))
        }
    }

    // [2021/12/13 14:13] ????????????????????????
    private fun setDropDownList() = loginLayout.apply {
        viewModel.userListData.clear()
        viewModel.userSplitContent.clear()
        val popupView = layoutInflater.inflate(R.layout.login_recyclerview, mBinding.root,false) //??????popuView???????????????
        val popupWindow = PopupWindow(popupView, longinUserCardView.width, 400) // ??????????????????popuWindow
        val loginListPop = popupView.findViewById(R.id.loginRecyclerView) as RecyclerView // ??????popupView????????????????????????rv??????
        val userName = getString(viewModel.userName) // ??????SP?????????userName
        if (userName.contains(",")) (viewModel.userSplitContent).plusAssign(userName.split(",").toMutableList())
        else viewModel.userSplitContent.add(userName)
        val size = viewModel.userSplitContent.size // ??????userSplistContent?????????
        repeat(size) { viewModel.userListData.add(LoginRecyclerViewAdapter.User(viewModel.userSplitContent[it])) } // userListData????????????
        val adapter = LoginRecyclerViewAdapter(viewModel.userListData) // ???????????????
        adapter.userInfoBlock = {
            loginPwd.loginEditText.text = null
            loginUser.loginEditText.setText(it.userName)
            loginUser.loginEditText.setSelection(loginUser.loginEditText.length())
            val pwd = getString("${viewModel.passWord}${it.userName}")
            if (pwd.isNotBlank()) loginPwd.loginEditText.setText(aes.decrypt(pwd, aes.loadKeyAES(AppData.AES_KEY)))
            popupWindow.dismiss()
        }
        adapter.deletBlock = {
            AlertDialog.Builder(this@LoginActivity).apply {
                setTitle("??????")
                setMessage("??????????????????????????????????????????")
                setIcon(R.drawable.tips)
                setCancelable(true)
                setPositiveButton("??????") { dialog, _ ->
                    val userData = getString(viewModel.userName)
                    removeValue(viewModel.userName)
                    removeValue("${viewModel.passWord}${it.userName}")
                    when {
                        userData.contains(",${it.userName}") -> { putString(viewModel.userName, userData.removeSuffix(",${it.userName}")) }
                        userData.contains("${it.userName},") -> { putString(viewModel.userName, userData.removePrefix("${it.userName},")) }
                        else -> loginUser.loginEndImage.visibility = View.GONE
                    }
                    popupWindow.dismiss()
                    dialog.dismiss()
                }
                setNegativeButton("??????") { dialog, _ -> dialog.dismiss() }
                show()
            }
        }
        loginListPop.layoutManager = LinearLayoutManager(this@LoginActivity)
        loginListPop.adapter = adapter
        loginListPop.background.alpha = 255
        popupWindow.isOutsideTouchable = true
        popupWindow.showAsDropDown(longinUserCardView, 0, 5)
        popupWindow.setOnDismissListener {
            viewModel.dropDownListStatus = false
            loginUser.loginEndImage.setImageResource(R.drawable.drop_downlist)
        }
    }
}