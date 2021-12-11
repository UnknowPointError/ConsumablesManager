package cn.example.consumablesManagement.view.activity

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.Nullable
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import cn.example.consumablesManagement.databinding.HomeActivityBinding
import cn.example.consumablesManagement.util.Loading
import cn.example.consumablesManagement.util.USERDATA
import cn.example.consumablesManagement.view.adapter.HomeViewPagerAdapter
import cn.example.consumablesManagement.view.fragment.ApplyFragment
import cn.example.consumablesManagement.view.fragment.ApplyViewModel
import cn.example.consumablesManagement.view.fragment.ConsuMablesFragment
import cn.example.consumablesManagement.view.fragment.UserFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.mob.tools.gui.ViewPagerAdapter
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {

    private val mBinding by lazy { HomeActivityBinding.inflate(layoutInflater) }
    private val fragment = arrayListOf(ConsuMablesFragment(), ApplyFragment(), UserFragment(this))
    private val tabItemList = listOf("耗材管理", "申请管理", "个人信息")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)
        initUI()
    }

    @SuppressLint("ResourceType")
    private fun initUI() = mBinding.apply {
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

