package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cn.example.consumablesManagement.databinding.HomeActivityBinding
import cn.example.consumablesManagement.view.adapter.HomeViewPagerAdapter
import cn.example.consumablesManagement.view.fragment.ApplyFragment
import cn.example.consumablesManagement.view.fragment.ConsuMablesFragment
import cn.example.consumablesManagement.view.fragment.UserFragment
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {

    private val mBinding by lazy { HomeActivityBinding.inflate(layoutInflater) }
    private val fragment = arrayListOf(ConsuMablesFragment(), ApplyFragment(), UserFragment(this))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initUI()
    }

    @SuppressLint("ResourceType")
    private fun initUI() = mBinding.apply {
        supportActionBar?.hide()
        val adapter = HomeViewPagerAdapter(this@HomeActivity, fragment)
        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "耗材管理"
                1 -> tab.text = "申请管理"
                2 -> tab.text = "个人信息"
            }
        }.attach()

    }
}

