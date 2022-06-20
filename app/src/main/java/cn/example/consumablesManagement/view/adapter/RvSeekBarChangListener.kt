package cn.example.consumablesManagement.view.adapter

import android.widget.SeekBar

/*************************
 * @ProjectName: ConsumablesManagement
 * @Dir_Path: app/src/main/java/cn/example/consumablesManagement/view/adapter
 * @Time: 2021 22:23 / 12月
 * @Author: BarryAllen
 * TODO: SeekBar事件封装
 **************************/
interface RvSeekBarChangListener : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        getProgress(progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    fun getProgress(progress: Int)
}