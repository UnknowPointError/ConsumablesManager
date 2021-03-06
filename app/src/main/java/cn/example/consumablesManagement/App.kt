package cn.example.consumablesManagement

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import leakcanary.AppWatcher
import leakcanary.LeakCanary

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement
 * @Time: 2021 11:46 / 12月
 * @Author: BarryAllen
 * TODO: Application 全局Context
 **************************/
class App : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        AppWatcher.appDefaultWatchers(this)
        LeakCanary.showLeakDisplayActivityLauncherIcon(true)
    }
}