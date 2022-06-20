package cn.example.consumablesManagement.view.adapter

import android.util.Log
import androidx.constraintlayout.motion.widget.MotionLayout

interface MotionListener : MotionLayout.TransitionListener {
    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
    }

    override fun onTransitionChange(
        motionLayout: MotionLayout?,
        startId: Int,
        endId: Int,
        progress: Float
    ) {
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout, currentId: Int) {
        motionFinish(motionLayout)
    }

    override fun onTransitionTrigger(
        motionLayout: MotionLayout?,
        triggerId: Int,
        positive: Boolean,
        progress: Float
    ) {
    }

    fun motionFinish(motionLayout: MotionLayout)
}