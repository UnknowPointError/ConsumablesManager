package cn.example.consumablesManagement.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.AddConsumablesBinding
import cn.example.consumablesManagement.databinding.FragmentApplyBinding
import cn.example.consumablesManagement.logic.model.entity.ConsumablesData
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.view.adapter.ApplyFragmentRvAdapter
import cn.example.consumablesManagement.view.adapter.RvAdapterSwipeListener
import java.io.File
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.databinding.ApplyConsumablesBinding
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.util.appUtil.StartActivityUtil.startActivity
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showToast
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.networkInterval
import cn.example.consumablesManagement.view.activity.ImageViewActivity
import cn.example.consumablesManagement.view.activity.viewModel.HomeViewModel
import cn.example.consumablesManagement.view.adapter.RvSeekBarChangListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.collections.ArrayList
import kotlin.concurrent.thread


// @formatter:off
@SuppressLint("InflateParams")
class ApplyFragment : Fragment() {

    inner class TakePicture : ActivityResultContracts.TakePicturePreview() {
        override fun createIntent(context: Context, input: Void?): Intent {
            super.createIntent(context, input)
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.imageUri)
            return intent
        }
    }

    private lateinit var picture : String
    private lateinit var mmBinding: AddConsumablesBinding
    private lateinit var mBinding: FragmentApplyBinding

    private lateinit var viewModel: HomeViewModel
    private val loadingTask = ConsumablesData(-1,"", -1,"","")
    private val loading = Loading()
    private val camera = registerForActivityResult(TakePicture()) {
        BitmapFactory.decodeStream(viewModel.imageUri?.let { uri -> activity?.contentResolver?.openInputStream(uri) })?.let {
            mmBinding.applyImageReView.visibility = View.VISIBLE
            val bytes = ByteArrayOutputStream()
            thread {
                it.compress(Bitmap.CompressFormat.PNG,50, bytes)
                picture = Base64.encodeToString(bytes.toByteArray(),Base64.DEFAULT)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        mBinding = FragmentApplyBinding.inflate(inflater, container, false)
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObserver()
        initView()
    }

    private fun initObserver() = mBinding.apply {
        viewModel = ViewModelProvider(activity!!).get(HomeViewModel::class.java)
        viewModel.responseBody.observe(viewLifecycleOwner) {
            if (it.code != AppData.ERROR_CODE) App.context.showToast(it.data.toString())
            if (it.code == AppData.NETWORKTAG_REMOVE) {
                viewModel.adapter?.csaData?.removeAt(viewModel.position!!)
                viewModel.adapter?.removeView()
                if (viewModel.adapter?.csaData?.size == 0) {
                    mBinding.applyNullCsa.visibility = View.VISIBLE // 解析后的耗材集合数据为0则显示“暂时没有库存”
                    mBinding.applyRecyclerView.visibility = View.GONE
                    mBinding.applySwipeRefresh.isRefreshing = false
                }
            }
        }
        viewModel.initRvData.observe(viewLifecycleOwner) {
            when {
                it.code == AppData.ERROR_CODE -> applySwipeRefresh.isRefreshing = false
                it.consumablesList?.size != 0 -> {
                    applyRecyclerView.visibility = View.VISIBLE
                    val consumablesData: ArrayList<ConsumablesData> = it.consumablesList!!
                    consumablesData.add(loadingTask) // 手动添加最后一行数据
                    viewModel.adapter = ApplyFragmentRvAdapter(consumablesData)
                    applyNullCsa.visibility = View.GONE // 耗材不为空，隐藏“暂时没有库存”文本
                    applyRecyclerView.layoutManager = LinearLayoutManager(activity)
                    applyRecyclerView.adapter = viewModel.adapter
                    viewModel.adapter!!.applyCsaApplicationClick = {
                        networkInterval {
                            val view = LayoutInflater.from(context!!).inflate(R.layout.apply_consumables, null)
                            val applicationBinding = ApplyConsumablesBinding.bind(view)
                            loading.loadingCustomDialog(view, R.style.CustomDialog2) {
                                applicationBinding.apply {
                                    val getProgress = object : RvSeekBarChangListener {
                                        override fun getProgress(progress: Int) {
                                            applySubmitText.text = progress.toString()
                                        }
                                    }
                                    applySubmitSeekBar.setOnSeekBarChangeListener(getProgress)
                                    applySubmitText.textSize = 20f
                                    applySubmitText.text = "0"
                                    applySubmitText.typeface = Typeface.createFromAsset(activity!!.assets, "fonts/HarmonyOS-Font.ttf")
                                    applySubmitButton.setOnClickListener {
                                        val submitText = applySubmitText.text.toString()
                                        when {
                                            submitText.toInt() > applyRvCsaCount.text.toString().toInt() ->
                                                App.context.showToast("你的申请的数量已超出库存数量！！！")
                                            submitText.toInt() == 0 ->
                                                App.context.showToast("申请的数量不能为0！")
                                            else -> {
                                                loading.cancelCustomDialog()
                                                val netWorkThread = NetworkThread(
                                                    AppData.userName,
                                                    uid = applyCsaUID.text.toString(),
                                                    csaName = applyRvCsaName.text.toString(),
                                                    csaCount = submitText,
                                                    url = AppData.HOME_APPLYCSA
                                                )
                                                viewModel.setResponseBody(true,activity!!,netWorkThread)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    viewModel.adapter!!.applyCsaRemoveClick = {
                        networkInterval {
                            viewModel.position = it
                            AlertDialog.Builder(context!!).apply {
                                setTitle("提示")
                                setMessage("确定删除此耗材吗？")
                                setCancelable(false)
                                setPositiveButton("确定") { _,_ ->
                                    val netWorkThread = NetworkThread(AppData.userName, csaName = applyRvCsaName.text.toString(),url = AppData.HOME_REMOVECSA)
                                    viewModel.setResponseBody(true,activity!!,netWorkThread)
                                }
                                setNegativeButton("取消") { dialog,_ -> dialog.dismiss() }
                                show()
                            }
                        }
                    }
                    setRecyclerViewListener(viewModel.adapter!!) // 设置Rv监听器
                    App.context.showToast("数据更新成功！")
                }
                else -> {
                    applyNullCsa.visibility = View.VISIBLE // 解析后的耗材集合数据为0则显示“暂时没有库存”
                    applyRecyclerView.visibility = View.GONE
                    applySwipeRefresh.isRefreshing = false
                }
            }
        }
        viewModel.loadingMoreData.observe(viewLifecycleOwner) { body ->
            if (body.code == AppData.ERROR_CODE) viewModel.adapter!!.remove() // 移除最后一行视图
            else body.consumablesList?.size?.let { size ->
                if (size != 0) { // 耗材数据长度不为0
                    val rvItemData: ArrayList<ConsumablesData> = arrayListOf()
                    val rowMinLimit = viewModel.adapter!!.itemCount // 获取Rv数据总数
                    /*这里传入rowMinLimit - 1 因为Rv最后一个数据是用来显示加载图标和文字，实际上显示耗材的数据大小就是itemCount - 1
                而数据库查询数据是通过LIMIT 5 OFFSET 接收的rowMinLimit，则限制了每次加载只会得到5行数据进行处理。
                例如数据库12行数据，首先初始化给Rv数据，得到5行实际的数据并手动添加一行数据用于“加载更多”，这时rowMintLimit = 5
                其次当滑到最后一行则rowMintLimit - 1 == 6 - 1，数据库接收到的rowMinLimit为5，并通过sql语句查询并找到第
                5--(固定值5->查找前5行数据）= 6 - 10行【总结：因为最后一行数据需要显示”正在加载“】。
                */
                    viewModel.adapter!!.loadingRvProgressBar?.visibility = View.VISIBLE // 设置正在加载图标可见
                    viewModel.adapter!!.loadingRvTextView?.text = getString(R.string.apply_loading) // 设置文本”正在加载...“
                    if (viewModel.adapter!!.csaData[rowMinLimit - 1].csaCount == -1) // Rv数据最后一行是否用于正在加载
                        viewModel.adapter!!.csaData.removeAt(rowMinLimit - 1) // 并移除
                    repeat(size) { rvItemData.add(body.consumablesList[it]) } // 添加耗材数据
                    rvItemData.add(loadingTask) // 实际上把刚移除的添加到最后一行
                    viewModel.adapter!!.addData(rvItemData) // 更新数据
                    App.context.showToast("加载成功！")
                } else { // 耗材数据长度 == 0
                    viewModel.adapter!!.remove() // 移除最后一行视图
                    App.context.showToast("暂无更多数据！")
                }
            }
        }
    }

    private fun initView() {
        mmBinding = AddConsumablesBinding.bind(LayoutInflater.from(context).inflate(R.layout.add_consumables, null))
        val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerLayout)
        mBinding.applyShowUser.setOnClickListener { actionInterval { drawerLayout.openDrawer(GravityCompat.START) } }
        mBinding.applyAddCSA.setOnClickListener {
            actionInterval {
                viewModel.imageUri = null
                mmBinding.apply {
                    loading.loadingCustomDialog(root,R.style.CustomDialog) {
                        applyAddSubmit.setOnClickListener { actionInterval { addCSAResult() } }
                        applyCsaCount.addTextChangedListener {
                            actionInterval {
                                viewModel.stringBuilder.clear()
                                val csaCountText = applyCsaCount.text.toString()
//                                if (csaCountText.isEmpty()) {
//                                    applyCsaCountFiled.isErrorEnabled = false
//                                    applyCsaCountFiled.error = null
//                                }
                                csaCountText.forEach {
                                    if (it < '0' || it > '9' && csaCountText.isNotBlank()) {
                                        viewModel.stringBuilder.append(it)
                                        applyCsaCountFiled.isErrorEnabled = true
                                        applyCsaCountFiled.error = getString(R.string.apply_errorStyly)
                                    } else {
                                        applyCsaCountFiled.isErrorEnabled = false
                                        applyCsaCountFiled.error = null
                                    }
                                }

                            }
                        }
                        applyCsaName.addTextChangedListener {
                            actionInterval {
                                applyCsaNameFiled.isErrorEnabled = false
                                applyCsaNameFiled.error = null
                            }
                        }
                        applyTakePicture.setOnClickListener {
                            actionInterval {
                                viewModel.outputImage = File(activity!!.externalCacheDir, "${AppData.userName}${System.currentTimeMillis()}.png")
                                viewModel.outputImage!!.delete().takeIf { viewModel.outputImage!!.exists() }
                                viewModel.outputImage!!.createNewFile()
                                val isVersionSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                val uri = if (isVersionSDK) FileProvider.getUriForFile(context, viewModel.authority.value!!, viewModel.outputImage!!)
                                else Uri.fromFile(viewModel.outputImage)
                                viewModel.imageUri = uri
                                camera.launch(null)
                            }
                        }
                        applyImageReView.setOnClickListener {
                            context!!.startActivity<ImageViewActivity> {
                                putExtra(AppData.IMAGE_URI,viewModel.imageUri)
                                putExtra(AppData.IMAGE_FILE_PATH,viewModel.outputImage!!.path)
                            }
                        }
                    }
                }
            }
        }
        mBinding.applySwipeRefresh.setOnRefreshListener {
            networkInterval {
                viewModel.setInitRvBody(false,activity!!,NetworkThread(rowMinLimit = "0", url = AppData.HOME_GETCSA))
            }
        }
        viewModel.setInitRvBody(true,activity!!,NetworkThread(rowMinLimit = "0", url = AppData.HOME_GETCSA))
    }


    // [2021/12/10 22:57] 设置RecyclerView的相关事件监听器
    private fun setRecyclerViewListener(adapter: ApplyFragmentRvAdapter) = mBinding.apply {
        applyRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() { //当Rv的数据显示或加载完成，则调用此函数
                applySwipeRefresh.isRefreshing = false // 取消上拉刷新的加载效果
                applyRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this) // 加载完成移除当前监听器
            }
        })
        applyRecyclerView.addOnScrollListener(object : RvAdapterSwipeListener(adapter) {
            override fun onSlideUpFinish() { // 当下滑到底部时则调用此函数，该监听器封装在adapter包下
                viewModel.setLoadingMoreBody(false,activity!!,NetworkThread(rowMinLimit = "${adapter.itemCount  - 1}", url = AppData.HOME_GETCSA))
            }
        })
    }

    // [2021/12/9 19:17] 添加耗材逻辑判断
    private fun addCSALogicJudge(csaName: String, csaCount: String): Boolean {
        mmBinding.apply {
            return if (csaName.isEmpty() or csaCount.isEmpty()) {
                if (csaName.isEmpty()) {
                    applyCsaNameFiled.isErrorEnabled = false
                    applyCsaNameFiled.error = getString(R.string.apply_errorCsaNameNull)
                }
                if (csaCount.isEmpty()) {
                    applyCsaCountFiled.isErrorEnabled = true
                    applyCsaCountFiled.error = getString(R.string.apply_errorCsaCountNull)
                }
                false
            } else {
                applyCsaNameFiled.isErrorEnabled = false
                applyCsaNameFiled.error = null
                applyCsaCountFiled.isErrorEnabled = false
                applyCsaCountFiled.error = null
                true
            }
        }
    }

    // [2021/12/9 19:17] 添加耗材并响应结果
    private fun addCSAResult() = mBinding.apply {
        val utf8 = StandardCharsets.UTF_8.toString()
        val csaName = URLEncoder.encode(mmBinding.applyCsaName.text.toString(),utf8)
        val csaCount = URLEncoder.encode(mmBinding.applyCsaCount.text.toString(),utf8)
        val csaUID = URLEncoder.encode(mmBinding.applyCsaUID.text.toString(),utf8)
        val errorResult = mmBinding.applyCsaCountFiled.isErrorEnabled
        if (addCSALogicJudge(csaName, csaCount) and !errorResult) {
            loading.cancelCustomDialog()
            loading.loadingDialog(context!!)
            var image = true
            if (viewModel.imageUri == null) image = false
            thread {
                val url = "${AppData.HOME_ADDCSA}?username=${AppData.userName}&uid=$csaUID&csaName=$csaName&csaCount=$csaCount&image=$image"
                val multipartBuilder= MultipartBody.Builder().setType(MultipartBody.FORM)
                viewModel.outputImage?.let {
                    multipartBuilder.addFormDataPart("png",it.name,it.asRequestBody("image/png".toMediaType()))
                } ?: multipartBuilder.addFormDataPart("","")
                val request = Request.Builder().url(url).post(multipartBuilder.build())
                    .build()
                val content = OkHttpClient().newCall(request).execute().body?.string()
                content?.let {
                    val gson = Gson()
                    val typeOf = object : TypeToken<ResponseBody>() {}.type
                    val result = gson.fromJson<ResponseBody>(it, typeOf)
                    activity!!.runOnUiThread {
                        loading.cancelDialog()
                        App.context.showToast(result.data.toString())
                    }
                }
            }
        }
    }

}

