package cn.example.consumablesManagement.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentSearchBinding
import cn.example.consumablesManagement.databinding.FragmentSearchRvBinding
import cn.example.consumablesManagement.logic.model.response.ConsumableSearchBody
import cn.example.consumablesManagement.logic.model.entity.ConsumablesSearchData
import cn.example.consumablesManagement.logic.model.response.ResponseBody
import cn.example.consumablesManagement.logic.network.NetworkThread
import cn.example.consumablesManagement.util.AppData
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showSnackBar
import cn.example.consumablesManagement.util.appUtil.TipsUtil.showToast
import cn.example.consumablesManagement.util.ktUtil.ActionTickUtil.actionInterval
import cn.example.consumablesManagement.util.ktUtil.NetWorkUtil.connectHttp
import cn.example.consumablesManagement.view.adapter.RvAdapterSwipeListener
import cn.example.consumablesManagement.view.adapter.SearchFragmentRvAdapter

// @formatter:off
class SearchFragment : Fragment() {

    private lateinit var mBinding : FragmentSearchBinding
    private val loading = Loading()
    private val loadingTask = ConsumablesSearchData("",-1, "",-1,"")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        mBinding = FragmentSearchBinding.bind(inflater.inflate(R.layout.fragment_search, container, false))
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.apply {
            val drawerLayout = activity!!.findViewById<DrawerLayout>(R.id.drawerLayout)
            searchAppBar.homeShowUser.setOnClickListener { actionInterval { drawerLayout.openDrawer(GravityCompat.START) } }
            loading.loadingDialog(context!!) // 开始加载
            searchSwipeRefresh.setOnRefreshListener{ actionInterval { initRecyclerView() } }
            initRecyclerView()
        }
    }

    // [2021/12/15 13:28] 初始化RecyclerView
    private fun initRecyclerView() = mBinding.apply{
        val networkThread = NetworkThread(rowMinLimit = "0", url = AppData.HOME_GETCSA_APPLY_INFO)
        connectHttp<ConsumableSearchBody>(4000,true,activity!!,networkThread,{
            if (consumablesSearchList.size > 0) {
                searchRv.visibility = View.VISIBLE
                val consumableSearchData : ArrayList<ConsumablesSearchData> = consumablesSearchList
                consumablesSearchList.add(loadingTask)
                val adapter = SearchFragmentRvAdapter(consumableSearchData)
                searchNullCsa.visibility = View.GONE
                searchRv.layoutManager = LinearLayoutManager(activity)
                searchRv.adapter = adapter
                adapter.csaRemoveApplyClick = { position -> actionInterval { setRvInsideUIAction(AppData.RV_REMOVE,this,adapter,position) } }
                adapter.csaAcceptApplyClick = { position -> actionInterval { setRvInsideUIAction(AppData.RV_ACCEPT,this,adapter,position) } }
                adapter.csaRefuseApplyClick = { position -> actionInterval { setRvInsideUIAction(AppData.RV_REFUSE,this,adapter,position) } }
                setRecyclerViewListener(adapter)
                loading.cancelDialog()
            } else {
                searchRv.visibility = View.GONE
                searchNullCsa.visibility = View.VISIBLE
                searchSwipeRefresh.isRefreshing = false
                loading.cancelDialog()
            }
            App.context.showToast("数据更新成功！")
        },{
            loading.cancelDialog()
            searchSwipeRefresh.isRefreshing = false
            App.context.showToast("网络异常")
        })
    }

    // [2021/12/15 13:28] 设置RecyclerView的相关事件监听器
    private fun setRecyclerViewListener(adapter: SearchFragmentRvAdapter) = mBinding.apply {
        searchRv.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            //当Rv的数据显示或加载完成，则调用此函数
            override fun onGlobalLayout(){
                loading.cancelDialog()
                searchSwipeRefresh.isRefreshing = false
                searchRv.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
        searchRv.addOnScrollListener(object : RvAdapterSwipeListener(searchAdapter = adapter) {
            override fun onSlideUpFinish(){
                val rowMinLimit = adapter.itemCount // 获取Rv数据总数
                val netWorkThread = NetworkThread(rowMinLimit = "${rowMinLimit - 1}", url = AppData.HOME_GETCSA_APPLY_INFO)
                connectHttp<ConsumableSearchBody>(4000,true,activity!!,netWorkThread, {
                    val rvItemData: ArrayList<ConsumablesSearchData> = arrayListOf()
                    val consumablesData = consumablesSearchList
                    val consumablesSize = consumablesData.size
                    if (consumablesSize != 0) { // 耗材数据长度不为0
                        adapter.loadingRvProgressBar?.visibility = View.VISIBLE // 设置正在加载图标可见
                        adapter.loadingRvTextView?.text = getString(R.string.apply_loading) // 设置文本”正在加载...“
                        if (adapter.csaData[rowMinLimit - 1].csaCount == -1) // Rv数据最后一行是否用于正在加载
                            adapter.csaData.removeAt(rowMinLimit - 1) // 并移除
                        repeat(consumablesSize) { rvItemData.add(consumablesData[it]) } // 添加耗材数据
                        rvItemData.add(loadingTask) // 实际上把刚移除的添加到最后一行
                        adapter.addData(rvItemData) // 更新数据
                        App.context.showToast("加载成功！")
                    } else { // 耗材数据长度 == 0
                        adapter.remove() // 移除最后一行视图
                        App.context.showToast("暂无更多数据！")
                    }
                }, {
                    adapter.remove() // 移除最后一行视图
                    root.showSnackBar("网络异常！")
                })
            }
        })
    }

    // [2021/12/21 17:41] 设置RecyclerView内部控件事件
    private fun setRvInsideUIAction(actionId: Int, mmBinding: FragmentSearchRvBinding, adapter: SearchFragmentRvAdapter,position: Int = 0) = mmBinding.apply {
        loading.loadingDialog(context!!)
        when(actionId) {
            AppData.RV_ACCEPT -> {
                val netWorkThread = NetworkThread(
                    userName = AppData.userName,
                    uid = searchCsaUID.text.toString(),
                    csaUserName = searchApplyUser.text.toString(),
                    csaName = searchCsaName.text.toString(),
                    csaCount = searchCsaCount.text.toString(),
                    time = searchApplyTime.text.toString(),
                    url = AppData.HOME_ACCEPT_APPLY)
                connectHttp<ResponseBody>(4000,true,activity!!,netWorkThread, {
                    App.context.showToast(data.toString())
                    if (code == 200) {
                        adapter.csaData.removeAt(position)
                        adapter.removeView()
                    }
                    loading.cancelDialog()
                }, {
                    loading.cancelDialog()
                })
            }
            AppData.RV_REFUSE -> {
                val netWorkThread = NetworkThread(
                    userName = AppData.userName,
                    csaUserName = searchApplyUser.text.toString(),
                    csaName = searchCsaName.text.toString(),
                    csaCount = searchCsaCount.text.toString(),
                    time = searchApplyTime.text.toString(),
                    url = AppData.HOME_REFUSE_APPLY
                )
                connectHttp<ResponseBody>(4000,true,activity!!,netWorkThread, {
                    App.context.showToast(data.toString())
                    if (code == 200) {
                        adapter.csaData.removeAt(position)
                        adapter.removeView()
                    }
                    loading.cancelDialog()
                } ,{
                    loading.cancelDialog()
                })
            }
            AppData.RV_REMOVE -> {
                val netWorkThread = NetworkThread(
                    userName = AppData.userName,
                    csaUserName = searchApplyUser.text.toString(),
                    csaName = searchCsaName.text.toString(),
                    csaCount = searchCsaCount.text.toString(),
                    time = searchApplyTime.text.toString(),
                    url = AppData.HOME_REMOVE_APPLY)
                connectHttp<ResponseBody>(4000,true,activity!!, netWorkThread, {
                    App.context.showToast(data.toString())
                    if (code == 200) {
                        adapter.csaData.removeAt(position)
                        adapter.removeView()
                    }
                    loading.cancelDialog()
                }, {
                    loading.cancelDialog()
                })
            }
        }
    }
}