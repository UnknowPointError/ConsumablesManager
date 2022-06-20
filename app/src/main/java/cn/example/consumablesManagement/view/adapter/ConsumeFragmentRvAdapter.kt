package cn.example.consumablesManagement.view.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentApplyLoadingBinding
import cn.example.consumablesManagement.databinding.FragmentConsumeRvBinding
import cn.example.consumablesManagement.logic.model.entity.ConsumablesUserData
import cn.example.consumablesManagement.util.AppData


class ConsumeFragmentRvAdapter (val csaData: ArrayList<ConsumablesUserData>) :
RecyclerView.Adapter<ConsumeFragmentRvAdapter.ViewHolder>() {

    var loadingRvTextView: TextView? = null
    var loadingRvProgressBar: ProgressBar? = null
    lateinit var consumeShowMoreListener : MotionLayout.(motionState : Int) -> Unit
    var isLoading = false

    companion object {
        const val NORMAL_VIEW = 0
        const val FOOT_VIEW = 1
    }

    inner class ViewHolder(
        val mBinding: FragmentConsumeRvBinding? = null,
        mmBinding: FragmentApplyLoadingBinding? = null,
    ) : RecyclerView.ViewHolder(mBinding?.root ?: mmBinding!!.root) {

        val csaName = mBinding?.consumeCsaName
        val csaCount = mBinding?.consumeCsaCount
        val csaUID = mBinding?.consumeCsaUID
        val csaTime = mBinding?.consumeTime
        val view = mBinding?.root
        init {
            // 默认加载不可见，以防当Rv不可滑动，数据并未占满屏幕时却显示正在加载
            loadingRvTextView?.visibility = View.GONE
            loadingRvProgressBar?.visibility = View.GONE
            csaName?.typeface = AppData.typeFonts
            csaCount?.typeface = AppData.typeFonts
            csaUID?.typeface = AppData.typeFonts
            csaTime?.typeface = AppData.typeFonts
            mBinding?.consumeShowTime?.typeface = AppData.typeFonts
            mBinding?.consumeShowCsaCount?.typeface = AppData.typeFonts
            mBinding?.consumeShowCsaUID?.typeface = AppData.typeFonts
            mBinding?.consumeMotionLayout?.setTransition(R.id.forward)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == FOOT_VIEW) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_apply_loading, parent, false)
            val mmBinding = FragmentApplyLoadingBinding.bind(view)
            loadingRvTextView = mmBinding.applyRvTextView
            loadingRvProgressBar = mmBinding.applyRvProgressBar
            ViewHolder(mmBinding = mmBinding)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_consume_rv, parent, false)
            val mBinding = FragmentConsumeRvBinding.bind(view)
            mBinding.consumeShowMore.setOnClickListener {mBinding.consumeMotionLayout.consumeShowMoreListener(mBinding.consumeMotionLayout.currentState) }
            ViewHolder(mBinding = mBinding)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val csa = csaData[position]
        holder.apply {
            csaName?.text = csa.csaName
            csaTime?.text = csa.time
            csaCount?.text = csa.csaCount.toString()
            csaUID?.text = csa.csaUID.toString()

        }
    }


    override fun getItemViewType(position: Int): Int {
        // 最后一行数据则 设置加载的正在加载布局的固定标识符
        return if (position == itemCount - 1 || position == 0 && csaData[0].csaCount == -1) {
            isLoading = true
            FOOT_VIEW
        } else {
            isLoading = false
            NORMAL_VIEW
        }
    }

    override fun getItemCount(): Int = csaData.size

    // [2021/12/11 20:05] 添加数据
    fun updateData(newData: ArrayList<ConsumablesUserData>) {
        val size = newData.size
        repeat(size) { csaData.add(newData[it]) }
        notifyItemChanged(csaData.size)
    }

    // [2021/12/11 20:05] 移除最后一个视图
    fun removeLastView() {
        loadingRvTextView?.visibility = View.GONE
        loadingRvProgressBar?.visibility = View.GONE
        notifyItemRemoved(itemCount)
    }

    fun removeView() {
        loadingRvTextView?.visibility = View.GONE
        loadingRvProgressBar?.visibility = View.GONE
        notifyDataSetChanged()
    }

    fun addView() = notifyItemMoved(csaData.size, 0)

}