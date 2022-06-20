package cn.example.consumablesManagement.view.fragment

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentConsumeBinding
import cn.example.consumablesManagement.logic.model.response.ConsumablesUserBody
import cn.example.consumablesManagement.logic.model.entity.ConsumablesUserData
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showSnackBar
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showToast
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil
import cn.example.consumablesManagement.view.activity.viewModel.HomeViewModel
import cn.example.consumablesManagement.view.adapter.ConsumeFragmentRvAdapter
import cn.example.consumablesManagement.view.adapter.MotionListener
import cn.example.consumablesManagement.view.adapter.RvAdapterSwipeListener

// @formatter:off
class ConsumeFragment : Fragment(){

    private lateinit var mBinding : FragmentConsumeBinding
    private val loading = Loading()
    private val loadingTask = ConsumablesUserData(-1,"",-1,"")
    private lateinit var viewModel: HomeViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = FragmentConsumeBinding.bind(inflater.inflate(R.layout.fragment_consume,container,false))

        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(activity!!).get(HomeViewModel::class.java)
        loading.loadingDialog(context!!)
        initRecyclerView()
        mBinding.apply {
            val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerLayout)
            consumeAppBar.homeShowUser.setOnClickListener { actionInterval { drawerLayout.openDrawer(GravityCompat.START) } }
            consumeSwipeRefresh.setOnRefreshListener { actionInterval { initRecyclerView() } }

        }
    }

    // [2021/12/23 11:58] 初始化Rv
    private fun initRecyclerView() = mBinding.apply {
        val networkThread = NetworkThread(AppData.userName,rowMinLimit = "0", url = AppData.HOME_GETCSA_USERDATA)
        NetWorkUtil.connectHttp<ConsumablesUserBody>(4000, false, activity!!, networkThread, {
            if (consumablesUserData.size != 0) {
                consumablesUserData.add(loadingTask) // 添加最后一行数据
                val adapter = ConsumeFragmentRvAdapter(consumablesUserData)
                consumeNull.visibility = View.GONE // 耗材不为空，隐藏“暂时没有库存”文本
                consumeRv.visibility = View.VISIBLE
                consumeRv.layoutManager = LinearLayoutManager(activity)
                consumeRv.adapter = adapter
                var motionStatus = false
                adapter.consumeShowMoreListener = {
                    if (it == startState)
                        transitionToEnd()
                    else
                        transitionToStart()
                }
                setRecyclerViewListener(adapter) // 设置Rv监听器
                App.context.showToast("数据更新成功！")
            } else {
                consumeNull.visibility = View.VISIBLE // 解析后的耗材集合数据为0则显示“暂时没有库存”
                consumeRv.visibility = View.GONE
                consumeSwipeRefresh.isRefreshing = false
                loading.cancelDialog() // 取消正在加载w
            }
        }, {
            consumeSwipeRefresh.isRefreshing = false
            loading.cancelDialog()
            root.showSnackBar("网络异常！")
        })
    }

    // [2021/12/23 11:57] 设置Rv相关监听事件
    private fun setRecyclerViewListener(adapter: ConsumeFragmentRvAdapter) = mBinding.apply{
        consumeRv.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() { //当Rv的数据显示或加载完成，则调用此函数
                loading.cancelDialog() // 取消加载
                consumeSwipeRefresh.isRefreshing = false // 取消上拉刷新的加载效果
                consumeRv.viewTreeObserver.removeOnGlobalLayoutListener(this) // 加载完成移除当前监听器
            }
        })
        consumeRv.addOnScrollListener(object : RvAdapterSwipeListener(consumeAdapter = adapter) {
            override fun onSlideUpFinish() { // 当下滑到底部时则调用此函数，该监听器封装在adapter包下
                val rowMinLimit = adapter.itemCount // 获取Rv数据总数
                val netWorkThread = NetworkThread(AppData.userName,rowMinLimit = "${rowMinLimit - 1}", url = AppData.HOME_GETCSA_USERDATA)
                val rvItemData: ArrayList<ConsumablesUserData> = arrayListOf()
                NetWorkUtil.connectHttp<ConsumablesUserBody>(4000, true, activity!!, netWorkThread, {
                    val consumablesSize = consumablesUserData.size
                    if (consumablesSize != 0) {
                        adapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置正在加载图标可见
                        adapter.loadingRvTextView?.text = getString(R.string.apply_loading) // 设置文本”正在加载...“
                        if (adapter.csaData[rowMinLimit - 1].csaCount == -1) adapter.csaData.removeAt(rowMinLimit - 1) // Rv数据最后一行是否用于正在加载 并移除
                        repeat(consumablesSize) { rvItemData.add(consumablesUserData[it]) } // 添加耗材数据
                        rvItemData.add(loadingTask) // 实际上把刚移除的添加到最后一行
                        adapter.updateData(rvItemData) // 更新数据
                        App.context.showToast("加载成功！")
                    } else {
                        adapter.removeLastView() // 移除最后一行视图
                        App.context.showToast("暂无更多数据！")
                    }
                }, {
                    adapter.removeLastView() // 移除最后一行视图
                    root.showSnackBar("网络异常！")
                })
            }
        })
    }
}