package cn.example.consumablesManagement.view.fragment

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.FragmentActivity
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.AddConsumablesBinding
import cn.example.consumablesManagement.databinding.FragmentApplyBinding
import cn.example.consumablesManagement.logic.model.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkSettings
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.Loading
import cn.example.consumablesManagement.util.Loading.reloadUI
import cn.example.consumablesManagement.util.Loading.reloading
import cn.example.consumablesManagement.util.TRYCATCH
import cn.example.consumablesManagement.util.Toasts.showSnackBar
import cn.example.consumablesManagement.util.USERDATA
import com.google.gson.Gson
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread

class ApplyFragment : Fragment() {

    inner class ApplyFragmentManager {

        private val context: Context by lazy { getContext() ?: context }
        private val activity: FragmentActivity by lazy { getActivity() ?: activity }

        fun initUI() {

        }

        fun initComponent() = mBinding.apply {
            applyAddCSA.setOnClickListener { addCSA() }
        }

        @SuppressLint("InflateParams")
        private fun addCSA() { // @formatter:off
            AddConsumablesBinding.bind(LayoutInflater.from(context).inflate(R.layout.add_consumables, null)).apply {
                root.reloadUI(R.style.CustomDialog) {
                    applyAddSubmit.setOnClickListener { addCSAResult() } // @formatter:on
                    applyCsaCount.addTextChangedListener { csaCountLogicJudge() }
                    applyCsaName.addTextChangedListener { csaNameLogicJudge() }
                }
            }
        }

        private fun AddConsumablesBinding.csaNameLogicJudge() {
            applyCsaNameFiled.isErrorEnabled = false
            applyCsaNameFiled.error = null
        }

        private fun AddConsumablesBinding.csaCountLogicJudge() {
            val csaCountText = applyCsaCount.text.toString()
            if (csaCountText.isEmpty()) {
                applyCsaCountFiled.isErrorEnabled = false
                applyCsaCountFiled.error = null
                return
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

        // @formatter:off
        private fun AddConsumablesBinding.addCSALogicJudge(csaName: String, csaCount: String): Boolean {
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

        // @formatter:on
        private fun AddConsumablesBinding.addCSAResult() {
            val csaName = this.applyCsaName.text.toString()
            val csaCount = this.applyCsaCount.text.toString()
            val errorResult = applyCsaCountFiled.isErrorEnabled
            if (addCSALogicJudge(csaName, csaCount) and !errorResult) {
                val task = FutureTask(NetworkThread( // @formatter:off
                    userName = USERDATA.userName,
                    csaName = csaName,
                    csaCount = csaCount, url = NetworkSettings.HOME_ADDCSA))
                Thread(task).start() // @formatter:on
                Loading.unloadUI()
                context.reloading()
                TRYCATCH.tryFunc({
                    thread {
                        val body = Gson().fromJson(task.get(), ResponseBody::class.java)
                        activity.runOnUiThread {
                            when (body.code) {
                                200 -> {
                                    mBinding.root.showSnackBar("添加成功")
                                }
                                500 -> {
                                    mBinding.root.showSnackBar("耗材已经存在！请刷新耗材列表！")
                                }
                                else -> {
                                    mBinding.root.showSnackBar("添加失败")
                                }
                            }
                            Loading.unloading()
                        }
                    }
                }, { Loading.unloading() })
            }
        }
    }

    companion object {
        fun newInstance() = ApplyFragment()
    }

    private lateinit var viewModel: ApplyViewModel
    private lateinit var mBinding: FragmentApplyBinding
    private val manager = ApplyFragmentManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, s: Bundle?): View {
        mBinding = FragmentApplyBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(ApplyViewModel::class.java)
        manager.initUI()
        manager.initComponent()
        // TODO: Use the ViewModel
    }

}