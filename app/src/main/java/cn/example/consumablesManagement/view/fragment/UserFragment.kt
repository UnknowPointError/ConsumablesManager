package cn.example.consumablesManagement.view.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.FragmentUserBinding
import cn.example.consumablesManagement.view.activity.HomeActivity
import cn.example.consumablesManagement.view.activity.LoginActivity
import com.google.android.material.appbar.AppBarLayout

class UserFragment(var asd: Any? = null) : Fragment() {

    companion object {
        fun newInstance() = UserFragment()
    }

    private lateinit var viewModel: UserViewModel
    private lateinit var mBinding: FragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentUserBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this).get(UserViewModel::class.java)
        mBinding.apply {
            userTextView.text = activity?.getSharedPreferences("user", Context.MODE_PRIVATE)
                ?.getString("LastLoginUser", "")
            userTextView.typeface =
                Typeface.createFromAsset(activity?.assets, "fonts/HarmonyOS-Font.ttf")
            val listener = AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
                val seekPosition = -verticalOffset / userAppBarLayout.totalScrollRange.toFloat()
                userMotionLayout.progress = seekPosition
            }
            userAppBarLayout.addOnOffsetChangedListener(listener)
            userExitBtn.setOnClickListener {
                activity?.finish()
                val intent = Intent(context, LoginActivity::class.java)
                intent.putExtra("ExitByUser", false)
                startActivity(intent)
            }
        }
    }
}