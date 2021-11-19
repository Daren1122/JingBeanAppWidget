package com.wj.jd

import android.app.Application
import com.tencent.bugly.crashreport.CrashReport

/**
 * author wangjing
 * Date 2021/6/25
 * Description
 */
class MyApplication : Application() {
    companion object {
        @JvmStatic
        lateinit var mInstance: MyApplication
    }

    override fun onCreate() {
        super.onCreate()
        mInstance = this

        CrashReport.initCrashReport(this, "26e85b4a9d", Constants.isDebug)
    }
}