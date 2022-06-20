package cn.example.consumablesManagement.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentApplyLoadingBinding
import cn.example.consumablesManagement.databinding.FragmentSearchRvBinding
import cn.example.consumablesManagement.logic.model.entity.ConsumablesSearchData
import cn.example.consumablesManagement.util.AppData

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/view/adapter
 * @Time: 2021 21:59 / 12月
 * @Author: BarryAllen
 * TODO: SearchFragment Rv适配器
 **************************/
class SearchFragmentRvAdapter(val csaData : ArrayList<ConsumablesSearchData>) : RecyclerView.Adapter<SearchFragmentRvAdapter.ViewHolder>(){

    inner class ViewHolder(
        val mBinding : FragmentSearchRvBinding? = null,
        mmBinding : FragmentApplyLoadingBinding? = null
    ) : RecyclerView.ViewHolder(mBinding?.root ?: mmBinding!!.root) {
        val csaName = mBinding?.searchCsaName
        val csaCount = mBinding?.searchCsaCount
        val csaTime = mBinding?.searchApplyTime
        val csaUser = mBinding?.searchApplyUser
        val csaUId = mBinding?.searchCsaUID
        val csaRemoveApply = mBinding?.searchRemoveApply
        val csaAcceptApply = mBinding?.searchAcceptApply
        val csaRefuseApply = mBinding?.searchRefuseApply
        val view = mBinding?.root
        init {
            // 默认加载不可见，以防当Rv不可滑动，数据并未占满屏幕时却显示正在加载
            loadingRvTextView?.visibility = View.GONE
            loadingRvProgressBar?.visibility = View.GONE
            csaName?.typeface = AppData.typeFonts
            csaCount?.typeface = AppData.typeFonts
            csaUser?.typeface = AppData.typeFonts
            csaTime?.typeface = AppData.typeFonts
            csaUId?.typeface = AppData.typeFonts
            mBinding?.searchShowTime?.typeface = AppData.typeFonts
            mBinding?.searchShowCsaUID?.typeface = AppData.typeFonts
            mBinding?.searchApplyShowUser?.typeface = AppData.typeFonts
            mBinding?.searchCsaShowCount?.typeface = AppData.typeFonts
        }
    }
    lateinit var csaRemoveApplyClick: FragmentSearchRvBinding.(position : Int) -> Unit
    lateinit var csaAcceptApplyClick: FragmentSearchRvBinding.(position : Int) -> Unit
    lateinit var csaRefuseApplyClick: FragmentSearchRvBinding.(position : Int) -> Unit
    var loadingRvTextView: TextView? = null
    var loadingRvProgressBar: ProgressBar? = null
    var isLoading = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == ApplyFragmentRvAdapter.FOOT_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_apply_loading, parent, false)
            val mmBinding = FragmentApplyLoadingBinding.bind(view)
            loadingRvTextView = mmBinding.applyRvTextView
            loadingRvProgressBar = mmBinding.applyRvProgressBar
            ViewHolder(mmBinding = mmBinding)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_search_rv, parent, false)
            ViewHolder(mBinding = FragmentSearchRvBinding.bind(view))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val csa = csaData[position]
        holder.apply {
            csaName?.text = csa.csaName
            csaUId?.text = csa.csaUID.toString()
            csaCount?.text = csa.csaCount.toString()
            csaUser?.text = csa.userName
            csaTime?.text = csa.time
            csaRemoveApply?.setOnClickListener { mBinding?.csaRemoveApplyClick(position) }
            csaAcceptApply?.setOnClickListener { mBinding?.csaAcceptApplyClick(position) }
            csaRefuseApply?.setOnClickListener { mBinding?.csaRefuseApplyClick(position) }
        }
    }


    override fun getItemViewType(position: Int): Int {
        // 最后一行数据则 设置加载的正在加载布局的固定标识符
        return if (position == itemCount - 1)  {
            isLoading = true
            ApplyFragmentRvAdapter.FOOT_VIEW
        } else {
            isLoading = false
            ApplyFragmentRvAdapter.NORMAL_VIEW
        }
    }
    override fun getItemCount(): Int = csaData.size

    // [2021/12/11 20:05] 添加数据
    fun addData(newData: ArrayList<ConsumablesSearchData>) {
        val size = newData.size
        repeat(size) { csaData.add(newData[it]) }
        notifyItemChanged(csaData.size)
    }

    // [2021/12/11 20:05] 移除最后一个视图
    fun remove() = notifyItemRemoved(itemCount)

    fun removeView() {
        loadingRvTextView?.visibility = View.GONE
        loadingRvProgressBar?.visibility = View.GONE
        notifyDataSetChanged()
    }
}