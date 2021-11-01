package com.wj.jd.util

import android.os.Handler
import android.os.Looper
import com.wj.jd.widget.WidgetUpdateDataUtil

/**
 * author wangjing
 * Date 2021/10/18
 * Description
 */
object UpdateTask {
    var widgetUpdateDataUtil = WidgetUpdateDataUtil()
    var widgetUpdateDataUtil1 = WidgetUpdateDataUtil()
    var widgetUpdateDataUtil2 = WidgetUpdateDataUtil()
    var widgetUpdateDataUtil3 = WidgetUpdateDataUtil()
    var widgetUpdateDataUtil4 = WidgetUpdateDataUtil()
    var widgetUpdateDataUtil5 = WidgetUpdateDataUtil()

    var handler = Handler(Looper.getMainLooper())

    fun updateAll() {
        handler.post {
            widgetUpdateDataUtil.updateWidget("ck")
        }
        handler.postDelayed({
            widgetUpdateDataUtil1.updateWidget("ck1")
        }, 2500)

        handler.postDelayed({
            widgetUpdateDataUtil2.updateWidget("ck2")
        }, 5000)

        handler.postDelayed({
            widgetUpdateDataUtil3.updateWidget("ck3")
        }, 7500)

        handler.postDelayed({
            widgetUpdateDataUtil4.updateWidget("ck4")
        }, 10000)

        handler.postDelayed({
            widgetUpdateDataUtil5.updateWidget("ck5")
        }, 12500)
    }
}