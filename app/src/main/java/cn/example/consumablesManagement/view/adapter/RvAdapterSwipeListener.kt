package cn.example.consumablesManagement.view.adapter

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.R

// @formatter:off
abstract class RvAdapterSwipeListener(
    private val applyAdapter: ApplyFragmentRvAdapter? = null,
    private val searchAdapter: SearchFragmentRvAdapter? = null,
    private val consumeAdapter: ConsumeFragmentRvAdapter? = null
) : RecyclerView.OnScrollListener() {
    private var isScrollup = false
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        val layout = recyclerView.layoutManager as LinearLayoutManager
        val lastPositionCompletely = layout.findLastVisibleItemPosition()
        if ((lastPositionCompletely == applyAdapter?.itemCount?.minus(1)) && newState == RecyclerView.SCROLL_STATE_IDLE && applyAdapter.itemCount > 2) {
            if (isScrollup) onSlideUpFinish() //滑到底部函数
        } else if ((lastPositionCompletely == searchAdapter?.itemCount?.minus(1)) && newState == RecyclerView.SCROLL_STATE_IDLE && searchAdapter.itemCount > 2) {
            if (isScrollup) onSlideUpFinish() //滑到底部函数
        } else if ((lastPositionCompletely == consumeAdapter?.itemCount?.minus(1)) && newState == RecyclerView.SCROLL_STATE_IDLE && consumeAdapter.itemCount > 2) {
            if (isScrollup) onSlideUpFinish() //滑到底部函数
        }
    }

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)
        if (dy > 0 && applyAdapter?.isLoading == true) { // 如果向上滑动屏幕条件则通过
            isScrollup = true
            applyAdapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置加载图标可见
            applyAdapter.loadingRvTextView?.visibility = View.VISIBLE // 设置”正在加载...“ 可见
            applyAdapter.loadingRvTextView?.text = App.context.getString(R.string.apply_loading) // 设置 ”正在加载...“ 内容
        } else if (dy > 0 && searchAdapter?.isLoading == true) { // 如果向上滑动屏幕条件则通过
            isScrollup = true
            searchAdapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置加载图标可见
            searchAdapter.loadingRvTextView?.visibility = View.VISIBLE // 设置”正在加载...“ 可见
            searchAdapter.loadingRvTextView?.text = App.context.getString(R.string.apply_loading) // 设置 ”正在加载...“ 内容
        } else if (dy > 0 && consumeAdapter?.isLoading == true) { // 如果向上滑动屏幕条件则通过
            isScrollup = true
            consumeAdapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置加载图标可见
            consumeAdapter.loadingRvTextView?.visibility = View.VISIBLE // 设置”正在加载...“ 可见
            consumeAdapter.loadingRvTextView?.text = App.context.getString(R.string.apply_loading) // 设置 ”正在加载...“ 内容
        }
    }

    protected abstract fun onSlideUpFinish()
}