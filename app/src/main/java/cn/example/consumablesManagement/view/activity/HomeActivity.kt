package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import cn.example.consumablesManagement.App
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.HomeActivityBinding
import cn.example.consumablesManagement.util.appUtil.Loading
import cn.example.consumablesManagement.view.activity.viewModel.HomeViewModel
import cn.example.consumablesManagement.view.adapter.HomeViewPagerAdapter
import cn.example.consumablesManagement.view.fragment.*
import com.google.android.material.tabs.TabLayoutMediator
import com.zackratos.ultimatebarx.ultimatebarx.statusBar
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    private val mBinding by lazy { HomeActivityBinding.inflate(layoutInflater) }
    private val fragment = arrayListOf(SearchFragment(), ApplyFragment(), ConsumeFragment())
    private val tabItemList = listOf("耗材管理", "申请管理", "消费记录")
    private val loading = Loading()
    private lateinit var viewModel : HomeViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initUI()
    }

    @SuppressLint("ResourceType")
    private fun initUI() = mBinding.apply {
        viewModel = ViewModelProvider(this@HomeActivity).get(HomeViewModel::class.java)
        statusBar {
            fitWindow = true
            drawableRes = R.color.homeBarColor
        }
        supportActionBar?.hide()
        val adapter = HomeViewPagerAdapter(this@HomeActivity, fragment)
        viewPager.offscreenPageLimit = 3
        viewPager.isUserInputEnabled = false
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = tabItemList[position]
                1 -> tab.text = tabItemList[position]
                2 -> tab.text = tabItemList[position]
            }
        }.attach()
    }

}

