package cn.example.consumablesManagement.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentApplyLoadingBinding
import cn.example.consumablesManagement.databinding.FragmentApplyRvBinding
import cn.example.consumablesManagement.logic.model.entity.ConsumablesData
import cn.example.consumablesManagement.util.AppData
import com.bumptech.glide.Glide

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/view/adapter
 * @Time: 2021 19:34 / 12月
 * @Author: BarryAllen
 * TODO: ApplyFragment RecyclerView Adapter
 **************************/
// @formatter:off
class ApplyFragmentRvAdapter(val csaData: ArrayList<ConsumablesData>) :
    RecyclerView.Adapter<ApplyFragmentRvAdapter.ViewHolder>() {

    lateinit var applyCsaApplicationClick: FragmentApplyRvBinding.() -> Unit
    lateinit var applyCsaRemoveClick: FragmentApplyRvBinding.(position: Int) -> Unit
    var loadingRvTextView: TextView? = null
    var loadingRvProgressBar: ProgressBar? = null
    var isLoading = false

    companion object {
        const val NORMAL_VIEW = 0
        const val FOOT_VIEW = 1
    }

    inner class ViewHolder(
        val mBinding: FragmentApplyRvBinding? = null,
        mmBinding: FragmentApplyLoadingBinding? = null,
    ) : RecyclerView.ViewHolder(mBinding?.root ?: mmBinding!!.root) {

        val csaName = mBinding?.applyRvCsaName
        val csaCount = mBinding?.applyRvCsaCount
        val csaUID = mBinding?.applyCsaUID
        val csaTime = mBinding?.applyTime
        val applyCsaBtn = mBinding?.applyRvApplicationBtn
        val applyRemoveBtn = mBinding?.applyRvRemoveBtn
        val applyImageView = mBinding?.applyRvImageView
        val view = mBinding?.root
        init {
            // 默认加载不可见，以防当Rv不可滑动，数据并未占满屏幕时却显示正在加载
            loadingRvTextView?.visibility = View.GONE
            loadingRvProgressBar?.visibility = View.GONE
            csaName?.typeface = AppData.typeFonts
            csaCount?.typeface = AppData.typeFonts
            csaUID?.typeface = AppData.typeFonts
            csaTime?.typeface = AppData.typeFonts
            mBinding?.applyShowTime?.typeface = AppData.typeFonts
            mBinding?.applyCsaUIDShow?.typeface = AppData.typeFonts
            mBinding?.applyRvCsaShowCount?.typeface = AppData.typeFonts
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == FOOT_VIEW) {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_apply_loading, parent, false)
            val mmBinding = FragmentApplyLoadingBinding .bind(view)
            loadingRvTextView = mmBinding.applyRvTextView
            loadingRvProgressBar = mmBinding.applyRvProgressBar
            ViewHolder(mmBinding = mmBinding)
        } else {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_apply_rv, parent, false)
            ViewHolder(mBinding = FragmentApplyRvBinding.bind(view))
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val csa = csaData[position]
        holder.apply {
            csaName?.text = csa.csaName
            csaTime?.text = csa.time
            csaCount?.text = csa.csaCount.toString()
            csaUID?.text = csa.csaUID.toString()
            applyImageView?.let {
                Glide.with(this.view!!)
                    .load(AppData.HOME_GETCSA_IMAGE.plus(csa.path))
                    .placeholder(R.drawable.cubes)
                    .into(it)
            }
            applyCsaBtn?.setOnClickListener { mBinding?.applyCsaApplicationClick() }
            applyRemoveBtn?.setOnClickListener { mBinding?.applyCsaRemoveClick(position) }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // 最后一行数据则 设置加载的正在加载布局的固定标识符
        return if (position == itemCount - 1 || position == 0 && csaData[0].csaCount == -1)  {
            isLoading = true
            FOOT_VIEW
        } else {
            isLoading = false
            NORMAL_VIEW
        }
    }

    override fun getItemCount(): Int = csaData.size

    // [2021/12/11 20:05] 添加数据
    fun addData(newData: ArrayList<ConsumablesData>) {
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

    fun addView() = notifyItemMoved(csaData.size,0)
}