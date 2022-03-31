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

    data class User(val userName: String, val userCancel: Int = R.drawable.trash)

    class ViewHolder(mBinding: LoginRecyclerviewItemBinding) : RecyclerView.ViewHolder(mBinding.root) {
        val userName = mBinding.loginTextItem
        val userCancel = mBinding.loginImageItem
        val view = mBinding.root
    }

    var userInfoBlock: (user: User) -> Unit = {}
    var deletBlock: (user: User) -> Unit = {}

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = userList[position]
        holder.userName.text = user.userName
        holder.userName.setOnClickListener { userInfoBlock(user) }
        holder.userCancel.setOnClickListener { deletBlock(user) }
    }

    override fun getItemCount(): Int = userList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LoginRecyclerviewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

}

