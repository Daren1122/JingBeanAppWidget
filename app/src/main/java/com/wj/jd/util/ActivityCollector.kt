package com.wj.jd.util

import android.app.Activity

/**
 * author wangjing
 * Date 2021/3/5
 * Description 全局管理activity
 */
object ActivityCollector {
    private val activities = ArrayList<Activity>()

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
    }

    fun exitApp() {
        finishAll()
    }
}