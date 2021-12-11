package cn.example.consumablesManagement.view.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import cn.example.consumablesManagement.util.Loading

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/view/adapter
 * @Time: 2021 15:25 / 12月
 * @Author: BarryAllen
 * TODO: HomeViewPagerAdapter
 **************************/
class HomeViewPagerAdapter(
    val fragmentActivity: FragmentActivity,
    private val fragment: ArrayList<Fragment>
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = fragment.size

    override fun createFragment(position: Int): Fragment {
        return fragment[position]
    }
}