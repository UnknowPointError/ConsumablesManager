package cn.example.consumablesManagement.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cn.example.consumablesManagement.R
import cn.example.consumablesManagement.databinding.LoginRecyclerviewItemBinding

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesmanagement
 * @Author: BarryAllen
 * @Time: 2021/11/17 9:37 星期三
 * TODO:
 **************************/
class LoginRecyclerViewAdapter(private val userList: ArrayList<User>) :
    RecyclerView.Adapter<LoginRecyclerViewAdapter.ViewHolder>() {

    var nameBlock: (user: User) -> Unit = {}
    var cancleBlock: (user: User) -> Unit = {}

    data class User(val userName: String, val userCancel: Int = R.drawable.trash)

    class ViewHolder(mBinding: LoginRecyclerviewItemBinding) :
        RecyclerView.ViewHolder(mBinding.root) {
        val userName = mBinding.loginTextItem
        val userCancel = mBinding.loginImageItem
        val view = mBinding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val mBinding =
            LoginRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(mBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.userName
        holder.userName.setOnClickListener { nameBlock(user) }
        holder.userCancel.setOnClickListener { cancleBlock(user) }
    }

    override fun getItemCount(): Int = userList.size

}

