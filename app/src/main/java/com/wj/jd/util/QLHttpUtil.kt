package com.wj.jd.util

import android.text.TextUtils
import android.util.Log
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.wj.jd.Constants

/**
 * author wangjing
 * Date 2021/9/17
 * Description 与青龙通讯类
 */
object QLHttpUtil {

    @JvmOverloads
    fun getQLToken(callback: StringCallBack?) {

    }

    public fun cancel(tag: Any) {
        OkGo.getInstance().cancelTag(tag)
    }

    public fun cancelAll() {
        OkGo.getInstance().cancelAll()
    }

}