package cn.example.consumablesManagement.view.fragment

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File

class ApplyViewModel : ViewModel() {
    // TODO: Implement the ViewModel
    private val _authority = MutableLiveData("cn.example.consumablesManagement.view.fragment")
    val authority: LiveData<String> get() = _authority

    private val _imageUri = MutableLiveData<Uri>()
    val imageUri: LiveData<Uri> get() = _imageUri

    private val _context = MutableLiveData<Context>()
    val context: LiveData<Context> get() = _context

    private val _activity = MutableLiveData<Activity>()
    val activity: LiveData<Activity> get() = _activity

    fun getImageUri(uri: Uri) {
        _imageUri.value = uri
    }

    fun setContext(context: Context) {
        _context.value = context
    }

    fun setActivity(activity: Activity){
        _activity.value = activity
    }
}