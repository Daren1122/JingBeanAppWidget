package com.wj.jd

import android.content.Intent
import android.text.TextUtils
import com.wj.jd.util.ActivityCollector
import com.wj.jd.util.UpdateTask

class EmptyActivity : BaseActivity() {

    override fun setLayoutId(): Int {
        return R.layout.activity_empty
    }

    override fun initView() {
        val ck = intent.getStringExtra("data")
        dealData(ck)
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val ck = intent.getStringExtra("data")
        dealData(ck)
    }

    private fun dealData(ck: String?) {
        if (TextUtils.isEmpty(ck)) return
        when (ck) {
            "ck" -> {
                UpdateTask.widgetUpdateDataUtil.updateWidget(ck)
            }
            "ck1" -> {
                UpdateTask.widgetUpdateDataUtil1.updateWidget(ck)
            }
            "ck2" -> {
                UpdateTask.widgetUpdateDataUtil2.updateWidget(ck)
            }
            "ck3" -> {
                UpdateTask.widgetUpdateDataUtil3.updateWidget(ck)
            }
            "ck4" -> {
                UpdateTask.widgetUpdateDataUtil4.updateWidget(ck)
            }
            "ck5" -> {
                UpdateTask.widgetUpdateDataUtil5.updateWidget(ck)
            }
        }
        ActivityCollector.finishAll()
    }

    override fun initData() {
    }

    override fun setEvent() {
    }
}