package cn.example.consumablesManagement.view.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.AttributeSet
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.widget.addTextChangedListener
import androidx.exifinterface.media.ExifInterface
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.AddConsumablesBinding
import cn.example.consumablesManagement.databinding.FragmentApplyBinding
import cn.example.consumablesManagement.logic.model.ConsumablesBody
import cn.example.consumablesManagement.logic.model.ConsumablesData
import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.Loading
import cn.example.consumablesManagement.util.NetWork
import cn.example.consumablesManagement.util.TryCatchUtil
import cn.example.consumablesManagement.util.ShowUtil.showSnackBar
import cn.example.consumablesManagement.util.USERDATA
import cn.example.consumablesManagement.view.adapter.ApplyFragmentRvAdapter
import cn.example.consumablesManagement.view.adapter.RecyclerViewSwipeListener
import java.io.File
import kotlin.concurrent.thread
import android.view.MotionEvent

import androidx.annotation.NonNull
import androidx.annotation.Nullable
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.util.ShowUtil.showToast


// @formatter:off
class ApplyFragment : Fragment() {

    inner class TakePicture : ActivityResultContracts.TakePicturePreview() {
        override fun createIntent(context: Context, input: Void?): Intent {
            super.createIntent(context, input)
            val intent = Intent("android.media.action.IMAGE_CAPTURE")
            intent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.imageUri.value)
            return intent
        }
    }

    private lateinit var mmBinding: AddConsumablesBinding
    private lateinit var mBinding: FragmentApplyBinding
    private lateinit var outputImage: File
    private val viewModel: ApplyViewModel by viewModels()
    private val loading = Loading()
    private val camera = registerForActivityResult(TakePicture()) {
        val bitmap =
            BitmapFactory.decodeStream(
                viewModel.activity.value!!.contentResolver?.openInputStream(
                    viewModel.imageUri.value!!
                )
            )
        bitmap?.let {
            mmBinding.applyImageView.setImageBitmap(rotateIfRequired(bitmap))
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        mBinding = FragmentApplyBinding.inflate(inflater, container, false)
        return mBinding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initData()
    }

    // [2021/12/9 19:19] Fragment初始化UI界面数据
    private fun initData() = mBinding.apply {
        viewModel.setActivity(activity!!)
        viewModel.setContext(context!!)
        loading.reloading(viewModel.context.value!!) // 开始加载
        applyAddCSA.setOnClickListener { addCsaUI() }
        applySwipeRefresh.setOnRefreshListener {
            initRecyclerView()
            App.context?.showToast("数据更新成功！")
        }
        initRecyclerView()
    }

    // [2021/12/10 22:57] 初始化RecyclerView相关控件及数据
    private fun initRecyclerView() = mBinding.apply {
        TryCatchUtil.tryCatch({
            thread {
                val networkThread = NetworkThread(rowMinLimit = "0", url = NetworkSettings.HOME_GETCSA)
                val body = NetWork.connect<ConsumablesBody>(networkThread) // 获取Gson文本解析后的数据
                viewModel.activity.value!!.runOnUiThread { // 主线程处理控件及数据
                    if (body.consumablesList.size != 0) {
                        val consumablesData: ArrayList<ConsumablesData> = body.consumablesList
                        consumablesData.add(ConsumablesData("",-1)) // 手动添加最后一行数据
                        val adapter = ApplyFragmentRvAdapter(consumablesData)
                        applyNullCsa.visibility = View.GONE // 耗材不为空，隐藏“暂时没有库存”文本
                        applyRecyclerView.layoutManager = LinearLayoutManager(viewModel.context.value)
                        applyRecyclerView.adapter = adapter
                        setRecyclerViewListener(adapter) // 设置Rv监听器
                    } else {
                        applyNullCsa.visibility = View.VISIBLE // 解析后的耗材集合数据为0则显示“暂时没有库存”
                        loading.unloading() // 取消正在加载
                    }
                }
            }
        }, {
            viewModel.activity.value!!.runOnUiThread {
                loading.unloading()
                applySwipeRefresh.isRefreshing = false
                root.showSnackBar("数据加载失败，请重新加载！")
            }
        })
    }
    
    // [2021/12/10 22:57] 设置RecyclerView的相关事件监听器 (包含上滑加载数据)
    private fun setRecyclerViewListener(adapter: ApplyFragmentRvAdapter) = mBinding.apply {
        applyRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            //当Rv的数据显示或加载完成，则调用此函数
            override fun onGlobalLayout() {
                loading.unloading() // 取消加载
                applySwipeRefresh.isRefreshing = false // 取消上拉刷新的加载效果
                applyRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this) // 加载完成移除当前监听器
            }
        })
        applyRecyclerView.addOnScrollListener(object : RecyclerViewSwipeListener(adapter, viewModel) {
            // 当下滑到底部时则调用此函数，该监听器封装在adapter包下
            override fun onSlideUpFinish(){
                thread { // 开启子线程避免Gson解析及网络连接卡住
                    /*这里传入rowMinLimit - 1 因为Rv最后一个数据是用来显示加载图标和文字，实际上显示耗材的数据大小就是itemCount - 1
                    而数据库查询数据是通过LIMIT 5 OFFSET 接收的rowMinLimit，则限制了每次加载只会得到5行数据进行处理。
                    例如数据库12行数据，首先初始化给Rv数据，得到5行实际的数据并手动添加一行数据用于“加载更多”，这时rowMintLimit = 5
                    其次当滑到最后一行则rowMintLimit - 1 == 6 - 1，数据库接收到的rowMinLimit为5，并通过sql语句查询并找到第
                    5--(固定值5->查找前5行数据）= 6 - 10行【总结：因为最后一行数据需要显示”正在加载“】。
                    */
                    TryCatchUtil.tryCatch({
                        val rowMinLimit = adapter.itemCount // 获取Rv数据总数
                        val netWorkThread = NetworkThread(rowMinLimit = "${rowMinLimit - 1}", url = NetworkSettings.HOME_GETCSA)
                        val rvItemData: ArrayList<ConsumablesData> = arrayListOf()
                        val body = NetWork.connect<ConsumablesBody>(netWorkThread) // 获取Gson解析后的数据
                        val consumablesData = body.consumablesList
                        val consumablesSize = consumablesData.size
                        if (consumablesSize != 0) { // 耗材数据长度不为0
                            viewModel.activity.value!!.runOnUiThread { // 主线程处理控件属性及数据
                                adapter.isLoadMore = true // 表示正在加载
                                adapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置正在加载图标可见
                                adapter.loadingRvTextView?.text = getString(R.string.apply_loading) // 设置文本”正在加载...“
                                if (adapter.csaData[rowMinLimit - 1].csaCount == -1) // Rv数据最后一行是否用于正在加载
                                    adapter.csaData.removeAt(rowMinLimit - 1) // 并移除
                                repeat(consumablesSize) { rvItemData.add(consumablesData[it]) } // 添加耗材数据
                                rvItemData.add(ConsumablesData("", -1)) // 实际上把刚移除的添加到最后一行
                                adapter.addData(rvItemData) // 更新数据
                                App.context?.showToast("更新成功！")
                            }
                        } else { // 耗材数据长度 == 0
                            viewModel.activity.value!!.runOnUiThread {
                                adapter.remove() // 移除最后一行视图
                                App.context?.showToast("暂无更多数据！")
                            }
                        }
                    })
                }
            }
        })
    }

    // [2021/12/9 19:18] 添加耗材UI界面及内部事件处理
    private fun addCsaUI(){
        mmBinding = AddConsumablesBinding.bind(LayoutInflater.from(context).inflate(R.layout.add_consumables, null)).apply {
            loading.reloadUI(root,R.style.CustomDialog) {
                applyAddSubmit.setOnClickListener { addCSAResult() } // @formatter:on
                applyCsaCount.addTextChangedListener {
                    val csaCountText = applyCsaCount.text.toString()
                    if (csaCountText.isEmpty()) {
                        applyCsaCountFiled.isErrorEnabled = false
                        applyCsaCountFiled.error = null
                    }
                    csaCountText.forEach {
                        if (it < '0' || it > '9') {
                            applyCsaCountFiled.isErrorEnabled = true
                            applyCsaCountFiled.error = getString(R.string.apply_errorStyly)
                        } else {
                            applyCsaCountFiled.isErrorEnabled = false
                            applyCsaCountFiled.error = null
                        }
                    }
                }
                applyCsaName.addTextChangedListener {
                    applyCsaNameFiled.isErrorEnabled = false
                    applyCsaNameFiled.error = null
                }
                applyTakePicture.setOnClickListener {
                    outputImage =
                        File(viewModel.activity.value!!.externalCacheDir, "output_image.jpg")
                    outputImage.delete().takeIf { outputImage.exists() }
                    outputImage.createNewFile()
                    val isVersionSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                    val uri = if (isVersionSDK) FileProvider.getUriForFile(
                        context,
                        viewModel.authority.value!!,
                        outputImage
                    )
                    else Uri.fromFile(outputImage) // @formatter:off
                    viewModel.getImageUri(uri)
                    camera.launch(null)
                }
            }
        }
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
        val csaName = mmBinding.applyCsaName.text.toString()
        val csaCount = mmBinding.applyCsaCount.text.toString()
        val errorResult = mmBinding.applyCsaCountFiled.isErrorEnabled
        val networkThread = NetworkThread(userName = USERDATA.userName, csaName = csaName, csaCount = csaCount, url = NetworkSettings.HOME_ADDCSA)
        if (addCSALogicJudge(csaName, csaCount) and !errorResult) {
            loading.unloadUI()
            loading.reloading(viewModel.context.value!!)
            TryCatchUtil.tryCatch({
                thread {
                    val body = NetWork.connect<ResponseBody>(networkThread)
                    viewModel.activity.value!!.runOnUiThread {
                        when (body.code) {
                            200 -> { root.showSnackBar("添加成功") }
                            500 -> { root.showSnackBar("耗材已经存在！请刷新耗材列表！") }
                            else -> { root.showSnackBar("添加失败") }
                        }
                        loading.unloading()
                    }
                }
            }, { loading.unloading() })
        }
    }

    // [2021/12/10 22:58] BitMap角度旋转判断及设置
    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage.path)
        return when (exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270)
            else -> bitmap
        }
    }

    /// [2021/12/10 22:59] 返回Bitmap旋转后的结果
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        Matrix().apply {
            postRotate(degree.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, true)
        }
    }

}

