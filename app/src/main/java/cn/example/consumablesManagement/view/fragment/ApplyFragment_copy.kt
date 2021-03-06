/*
package cn.example.consumablesManagement.view.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.*
import androidx.fragment.app.Fragment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.MotionScene
import androidx.core.content.FileProvider
import androidx.core.view.ContentInfoCompat
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.core.widget.addTextChangedListener
import androidx.drawerlayout.widget.DrawerLayout
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.AddConsumablesBinding
import cn.example.consumablesManagement.databinding.FragmentApplyBinding
import cn.example.consumablesManagement.logic.model.response.ConsumablesBody
import cn.example.consumablesManagement.logic.model.entity.ConsumablesData
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showSnackBar
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.view.adapter.ApplyFragmentRvAdapter
import cn.example.consumablesManagement.view.adapter.RvAdapterSwipeListener
import java.io.File
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.databinding.ApplyConsumablesBinding
import cn.example.consumablesManagement.databinding.FragmentApplyRvBinding
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.util.appUtil.StartActivityUtil.startActivity
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showToast
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil.connectHttp
import cn.example.consumablesManagement.view.activity.ImageViewActivity
import cn.example.consumablesManagement.view.activity.viewModel.HomeViewModel
import cn.example.consumablesManagement.view.adapter.MotionListener
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
class xApplyFragment_copy : Fragment() {

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
    private var outputImage: File? = null
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
        viewModel = ViewModelProvider(activity!!).get(HomeViewModel::class.java)
        mBinding.apply {
            val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerLayout)
            applyShowUser.setOnClickListener { actionInterval { drawerLayout.openDrawer(GravityCompat.START) } }
            loading.loadingDialog(context!!)
            applyAddCSA.setOnClickListener { actionInterval { addCsaUI() } }
            applySwipeRefresh.setOnRefreshListener { actionInterval { initRecyclerView() } }
            initRecyclerView()
        }
    }


    // [2021/12/9 19:18] ????????????UI???????????????????????????
    private fun addCsaUI(){
        viewModel.imageUri = null
        mmBinding = AddConsumablesBinding.bind(LayoutInflater.from(context).inflate(R.layout.add_consumables, null)).apply {
            loading.loadingCustomDialog(root,R.style.CustomDialog) {
                applyAddSubmit.setOnClickListener { actionInterval { addCSAResult() } }
                applyCsaCount.addTextChangedListener {
                    actionInterval {
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
                }
                applyCsaName.addTextChangedListener {
                    actionInterval {
                        applyCsaNameFiled.isErrorEnabled = false
                        applyCsaNameFiled.error = null
                    }
                }
                applyTakePicture.setOnClickListener {
                    actionInterval {
                        outputImage = File(activity!!.externalCacheDir, "${AppData.userName}${System.currentTimeMillis()}.png")
                        outputImage!!.delete().takeIf { outputImage!!.exists() }
                        outputImage!!.createNewFile()
                        val isVersionSDK = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        val uri = if (isVersionSDK) FileProvider.getUriForFile(
                            context,
                            viewModel.authority.value!!,
                            outputImage!!
                        )
                        else Uri.fromFile(outputImage)
                        viewModel.imageUri = uri
                        camera.launch(null)
                    }
                }
                applyImageReView.setOnClickListener {
                    context!!.startActivity<ImageViewActivity> {
                        putExtra(AppData.IMAGE_URI,viewModel.imageUri)
                    }
                }
            }
        }
    }

    // [2021/12/10 22:57] ?????????RecyclerView?????????????????????
    private fun initRecyclerView() = mBinding.apply {
        val networkThread = NetworkThread(rowMinLimit = "0", url = AppData.HOME_GETCSA)
        connectHttp<ConsumablesBody>(4000, false, activity!!, networkThread, {
            if (consumablesList.size != 0) {
                applyRecyclerView.visibility = View.VISIBLE
                val consumablesData: ArrayList<ConsumablesData> = consumablesList
                consumablesData.add(loadingTask) // ??????????????????????????????
                val adapter = ApplyFragmentRvAdapter(consumablesData)
                applyNullCsa.visibility = View.GONE // ??????????????????????????????????????????????????????
                applyRecyclerView.layoutManager = LinearLayoutManager(activity)
                applyRecyclerView.adapter = adapter
                adapter.applyCsaApplicationClick = { actionInterval { setRvInsideUIAction(AppData.RV_APPLICATION, this,adapter) } }
                adapter.applyCsaRemoveClick = { actionInterval { setRvInsideUIAction(AppData.RV_REMOVE, this,adapter,it) } }
                setRecyclerViewListener(adapter) // ??????Rv?????????
                App.context.showToast("?????????????????????")
            } else {
                applyNullCsa.visibility = View.VISIBLE // ?????????????????????????????????0?????????????????????????????????
                applyRecyclerView.visibility = View.GONE
                loading.cancelDialog() // ??????????????????
                applySwipeRefresh.isRefreshing = false
            }
        }, {
            loading.cancelDialog()
            applySwipeRefresh.isRefreshing = false
            root.showSnackBar("???????????????")
        })
    }

    // ??????Rv??????????????????
    private fun setRvInsideUIAction(actionId: Int, mmBinding: FragmentApplyRvBinding, adapter: ApplyFragmentRvAdapter,position: Int = 0) = mmBinding.apply {
        when (actionId) {
            AppData.RV_APPLICATION -> {
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
                            if (submitText.toInt() > applyRvCsaCount.text.toString().toInt()) {
                                App.context.showToast("???????????????????????????????????????????????????")
                            } else if (submitText.toInt() == 0) {
                                App.context.showToast("????????????????????????0???")
                            } else {
                                loading.cancelCustomDialog()
                                val netWorkThread = NetworkThread(
                                    AppData.userName,
                                    uid = applyCsaUID.text.toString(),
                                    csaName = applyRvCsaName.text.toString(),
                                    csaCount = submitText,
                                    url = AppData.HOME_APPLYCSA
                                )
                                loading.loadingDialog(context!!)
                                connectHttp<ResponseBody>(4000, true, activity!!, netWorkThread, {
                                        loading.cancelDialog()
                                        App.context.showToast(data.toString())
                                }, {
                                    loading.cancelDialog()
                                    App.context.showToast("???????????????")
                                })
                            }
                        }
                    }
                }
            }
            AppData.RV_REMOVE -> {
                AlertDialog.Builder(context!!).apply {
                    setTitle("??????")
                    setMessage("???????????????????????????")
                    setCancelable(false)
                    setPositiveButton("??????") { dialog,which ->
                        loading.loadingDialog(context!!)
                        val netWorkThread = NetworkThread(AppData.userName, csaName = applyRvCsaName.text.toString(),url = AppData.HOME_REMOVECSA)
                        connectHttp<ResponseBody>(4000,true,activity!!, netWorkThread, {
                            context.showToast(data.toString())
                            if (code == 200) {
                                adapter.csaData.removeAt(position)
                                adapter.removeView()
                                if (adapter.csaData.size == 0) {
                                    mBinding.applyNullCsa.visibility = View.VISIBLE // ?????????????????????????????????0?????????????????????????????????
                                    mBinding.applyRecyclerView.visibility = View.GONE
                                    mBinding.applySwipeRefresh.isRefreshing = false
                                }
                            }
                            loading.cancelDialog()
                        }, {
                            loading.cancelDialog()
                        })
                    }
                    setNegativeButton("??????") { dialog,which ->
                        dialog.dismiss()
                    }
                    show()
                }
            }
        }
    }

    // [2021/12/10 22:57] ??????RecyclerView????????????????????????
    private fun setRecyclerViewListener(adapter: ApplyFragmentRvAdapter) = mBinding.apply {
        applyRecyclerView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            //???Rv???????????????????????????????????????????????????
            override fun onGlobalLayout() {
                loading.cancelDialog() // ????????????
                applySwipeRefresh.isRefreshing = false // ?????????????????????????????????
                applyRecyclerView.viewTreeObserver.removeOnGlobalLayoutListener(this) // ?????????????????????????????????
            }
        })
        applyRecyclerView.addOnScrollListener(object : RvAdapterSwipeListener(adapter) {
            // ???????????????????????????????????????????????????????????????adapter??????
            override fun onSlideUpFinish() {
                */
/*????????????rowMinLimit - 1 ??????Rv???????????????????????????????????????????????????????????????????????????????????????????????????itemCount - 1
                ?????????????????????????????????LIMIT 5 OFFSET ?????????rowMinLimit???????????????????????????????????????5????????????????????????
                ???????????????12??????????????????????????????Rv???????????????5??????????????????????????????????????????????????????????????????????????????rowMintLimit = 5
                ??????????????????????????????rowMintLimit - 1 == 6 - 1????????????????????????rowMinLimit???5????????????sql????????????????????????
                5--(?????????5->?????????5????????????= 6 - 10???????????????????????????????????????????????????????????????????????????
                *//*

                val rowMinLimit = adapter.itemCount // ??????Rv????????????
                val netWorkThread =
                    NetworkThread(rowMinLimit = "${rowMinLimit - 1}", url = AppData.HOME_GETCSA)
                connectHttp<ConsumablesBody>(4000, true, activity!!, netWorkThread, {
                    val rvItemData: ArrayList<ConsumablesData> = arrayListOf()
                    val consumablesSize = consumablesList.size
                    if (consumablesSize != 0) { // ????????????????????????0
                        adapter.loadingRvProgressBar?.visibility = View.VISIBLE // ??????????????????????????????
                        adapter.loadingRvTextView?.text =
                            getString(R.string.apply_loading) // ???????????????????????????...???
                        if (adapter.csaData[rowMinLimit - 1].csaCount == -1) // Rv??????????????????????????????????????????
                            adapter.csaData.removeAt(rowMinLimit - 1) // ?????????
                        repeat(consumablesSize) { rvItemData.add(consumablesList[it]) } // ??????????????????
                        rvItemData.add(loadingTask) // ?????????????????????????????????????????????
                        adapter.addData(rvItemData) // ????????????
                        App.context.showToast("???????????????")
                    } else { // ?????????????????? == 0
                        adapter.remove() // ????????????????????????
                        App.context.showToast("?????????????????????")
                    }
                }, {
                    adapter.remove() // ????????????????????????
                    root.showSnackBar("???????????????")
                })
            }
        })
    }

    // [2021/12/9 19:17] ????????????????????????
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

    // [2021/12/9 19:17] ???????????????????????????
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
                outputImage?.let {
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

    // [2021/12/10 22:58] BitMap???????????????????????????
    private fun rotateIfRequired(bitmap: Bitmap): Bitmap {
        val exif = ExifInterface(outputImage!!.path)
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

    /// [2021/12/10 22:59] ??????Bitmap??????????????????
    private fun rotateBitmap(bitmap: Bitmap, degree: Int): Bitmap {
        Matrix().apply {
            postRotate(degree.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, this, true)
        }
    }

}

*/
