package com.wj.jd.util

import android.text.TextUtils
import com.wj.jd.MyApplication

/**
 * author wangjing
 * Date 2021/5/31
 * Description 页面缓存和个人信息缓存
 */
object CacheUtil {
    private const val PREFS = "page_cache_config"

    fun getString(key: String?): String? {
        return SharedPreferencesUtils.getPrefs(MyApplication.mInstance, PREFS).getString(key, "")
    }

    fun putString(key: String?, value: String?) {
        SharedPreferencesUtils.getEditor(MyApplication.mInstance, PREFS)
            .putString(key, value).commit()
    }

    fun getCKPtPin(key: String): String {
        try {
            val ck = getString(key)
            if (TextUtils.isEmpty(ck)) {
                return ""
            }
            var pin = ck!!.substring(ck.indexOf("pt_pin="))
            pin = pin.substring(0, pin.indexOf(";"))
            return pin
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return key
    }
}