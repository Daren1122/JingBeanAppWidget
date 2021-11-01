package com.wj.jd.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.gson.Gson
import com.wj.jd.MainActivity
import com.wj.jd.MyApplication
import com.wj.jd.bean.JingDouBean
import com.wj.jd.bean.RedPacket
import com.wj.jd.bean.UserBean
import com.wj.jd.bean.VersionBean
import com.wj.jd.util.*
import com.wj.jd.util.TimeUtil.getCurrentData
import com.wj.jd.util.TimeUtil.getCurrentHH
import com.wj.jd.util.TimeUtil.parseTime
import org.json.JSONObject
import java.lang.Exception

import android.graphics.BitmapFactory
import android.graphics.Color
import com.wj.jd.R

/**
 * author wangjing
 * Date 2021/10/13
 * Description
 */
class WidgetUpdateDataUtil {
    private lateinit var remoteViews: RemoteViews
    private var gson = Gson()
    private var todayTime: Long = 0
    private var yesterdayTime: Long = 0
    lateinit var thisKey: String
    private lateinit var userBean: UserBean

    @Synchronized
    fun updateWidget(key: String) {
        if (TimeUtil.isFastClick()) {
            return
        }

        thisKey = key
        val str = HttpUtil.getCK(thisKey)
        if (TextUtils.isEmpty(str)) return

        userBean = UserBean()

        remoteViews = RemoteViews(MyApplication.mInstance.packageName, R.layout.widges_layout)
        remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_15.dmToPx(), 0, R.dimen.dp_15.dmToPx(), 0)
        pullWidget()

        checkUpdate()

        getUserInfo()
        getUserInfo1()

        todayTime = TimeUtil.getTodayMillis(0)
        yesterdayTime = TimeUtil.getTodayMillis(-1)

        getJingBeanData()

        getRedPackge()
    }

    private fun checkUpdate() {
        HttpUtil.getAppVer(object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    var gson = Gson()
                    val versionBean = gson.fromJson(result, VersionBean::class.java)
                    if (DeviceUtil.getAppVersionName().equals(versionBean.release)) {
                        userBean.updateTips = ""
                    } else {
                        userBean.updateTips = versionBean.widgetTip
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }

        })
    }

    private fun getRedPackge() {
        HttpUtil.getRedPack(thisKey, "https://m.jingxi.com/user/info/QueryUserRedEnvelopesV2?type=1&orgFlag=JD_PinGou_New&page=1&cashRedType=1&redBalanceFlag=1&channel=1&_=" + System.currentTimeMillis() + "&sceneval=2&g_login_type=1&g_ty=ls", object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    val redPacket = gson.fromJson(result, RedPacket::class.java)
                    userBean.hb = redPacket.data.balance
                    userBean.gqhb = redPacket.data.expiredBalance
                    userBean.countdownTime = redPacket.data.countdownTime / 60 / 60
                    setData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }
        })
    }

    private fun getUserInfo1() {
        HttpUtil.getUserInfo1(thisKey, object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    val job = JSONObject(result)
                    userBean.jxiang = job.optJSONObject("user").optString("uclass").replace("京享值", "")
                    setData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {

            }
        })
    }

    private fun getUserInfo() {
        HttpUtil.getUserInfo(thisKey, object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    val job = JSONObject(result)
                    try {
                        userBean.nickName = job.optJSONObject("data").optJSONObject("userInfo").optJSONObject("baseInfo").optString("nickname")
                        userBean.userLevel = job.optJSONObject("data").optJSONObject("userInfo").optJSONObject("baseInfo").optString("userLevel")
                        userBean.levelName = job.optJSONObject("data").optJSONObject("userInfo").optJSONObject("baseInfo").optString("levelName")
                        userBean.headImageUrl = job.optJSONObject("data").optJSONObject("userInfo").optJSONObject("baseInfo").optString("headImageUrl")
                        userBean.isPlusVip = job.optJSONObject("data").optJSONObject("userInfo").optString("isPlusVip")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    try {
                        userBean.beanNum = job.optJSONObject("data").optJSONObject("assetInfo").optString("beanNum")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    setData()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }

        })
    }

    @Synchronized
    private fun getJingBeanData() {
        HttpUtil.getJD(thisKey, userBean.page, object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    Log.i("====", result)
                    val jingDouBean = gson.fromJson(result, JingDouBean::class.java)
                    val dataList = jingDouBean.detailList
                    var isFinish = true
                    for (i in dataList.indices) {
                        val detail = dataList[i]
                        val beanDay = parseTime(detail.date)!!
                        if (beanDay > todayTime) {
                            if (detail.amount > 0) {
                                userBean.todayBean = userBean.todayBean + detail.amount
                            }
                        } else {
                            isFinish = false
                            break
                        }
                    }
                    if (isFinish) {
                        userBean.page++
                        getJingBeanData()
                    } else {
                        Log.i("====", TimeUtil.getYesterDay(-1))
                        var oneAgoJBeanNum = CacheUtil.getString(TimeUtil.getYesterDay(-1) + thisKey)
                        if (TextUtils.isEmpty(oneAgoJBeanNum) || 0 == oneAgoJBeanNum?.toInt()) {
                            Log.i("====", "昨天缓存数据为空 请求后台")
                            get1AgoBeanData()
                        } else {
                            Log.i("====", "使用缓存数据")
                            userBean.ago1Bean = oneAgoJBeanNum?.toInt()!!
                        }
                        setData()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }
        })
    }

    private fun get1AgoBeanData() {
        HttpUtil.getJD(thisKey, userBean.page, object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    val jingDouBean = gson.fromJson(result, JingDouBean::class.java)
                    val dataList = jingDouBean.detailList
                    var isFinish = true
                    for (i in dataList.indices) {
                        val detail = dataList[i]
                        val beanDay = parseTime(detail.date)!!
                        if (beanDay in (yesterdayTime + 1) until todayTime) {
                            if (detail.amount > 0) {
                                userBean.ago1Bean = userBean.ago1Bean + detail.amount
                            }
                        } else if (beanDay < yesterdayTime) {
                            isFinish = false
                            break
                        }
                    }
                    if (isFinish) {
                        userBean.page++
                        get1AgoBeanData()
                    } else {
                        CacheUtil.putString(TimeUtil.getYesterDay(-1) + thisKey, userBean.ago1Bean.toString())
                        setData()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }
        })
    }

    private fun setData() {
        if ("1" == CacheUtil.getString("hideTips")) {
            remoteViews.setViewVisibility(R.id.updateTime, View.GONE)
            remoteViews.setViewVisibility(R.id.tips, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.updateTime, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.tips, View.VISIBLE)
        }

        if ("1" == CacheUtil.getString("hideNichen")) {
            remoteViews.setTextViewText(R.id.nickName, "***")
        } else {
            remoteViews.setTextViewText(R.id.nickName, userBean.nickName)
        }

        if ("1" == userBean.isPlusVip) {
            remoteViews.setViewVisibility(R.id.plusIcon, View.VISIBLE)
        } else {
            remoteViews.setViewVisibility(R.id.plusIcon, View.GONE)
        }

        if (TextUtils.isEmpty(userBean.updateTips)) {
            remoteViews.setViewVisibility(R.id.haveNewVersion, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.haveNewVersion, View.VISIBLE)
            remoteViews.setTextViewText(R.id.haveNewVersion, userBean.updateTips)
        }

        val paddingType = CacheUtil.getString("paddingType")
        if (TextUtils.isEmpty(paddingType) || "15dp" == paddingType) {
            remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_15.dmToPx(), 0, R.dimen.dp_15.dmToPx(), 0)
        } else if ("无边距" == paddingType) {
            remoteViews.setViewPadding(R.id.rootParent, 0, 0, 0, 0)
        } else if ("5dp" == paddingType) {
            remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_5.dmToPx(), 0, R.dimen.dp_5.dmToPx(), 0)
        } else if ("10dp" == paddingType) {
            remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_10.dmToPx(), 0, R.dimen.dp_10.dmToPx(), 0)
        } else if ("20dp" == paddingType) {
            remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_20.dmToPx(), 0, R.dimen.dp_20.dmToPx(), 0)
        }

        var designColor = CacheUtil.getString("designColor")
        if (TextUtils.isEmpty(designColor)) {
            designColor = "#FFFFFF"
        }
        val bac = BitmapUtil.getColorBitmap(designColor)
        remoteViews.setImageViewBitmap(R.id.background, BitmapUtil.bimapRound(bac, 20f))

        val colorSwitch = CacheUtil.getString("colorSwitch")
        if ("1" == colorSwitch) {
            remoteViews.setTextColor(R.id.nickName, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.jingXiang, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.beanNum, ColorUtil.transColor("#FF0000"))
            remoteViews.setTextColor(R.id.todayBean, ColorUtil.transColor("#008000"))
            remoteViews.setTextColor(R.id.yesterDayTip, ColorUtil.transColor("#555555"))
            remoteViews.setTextColor(R.id.todayTip, ColorUtil.transColor("#555555"))
            remoteViews.setTextColor(R.id.oneAgoBeanNum, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.todayBeanNum, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.title, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.guoquHb, ColorUtil.transColor("#555555"))
            remoteViews.setTextColor(R.id.hongbao, ColorUtil.transColor("#FF0000"))
            remoteViews.setTextColor(R.id.hongbao, ColorUtil.transColor("#FF0000"))
            remoteViews.setTextColor(R.id.tips, ColorUtil.transColor("#555555"))
            remoteViews.setTextColor(R.id.updateTime, ColorUtil.transColor("#555555"))
        } else {
            remoteViews.setTextColor(R.id.nickName, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.jingXiang, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.beanNum, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.todayBean, Color.parseColor("#008000"))
            remoteViews.setTextColor(R.id.yesterDayTip, Color.parseColor("#555555"))
            remoteViews.setTextColor(R.id.todayTip, Color.parseColor("#555555"))
            remoteViews.setTextColor(R.id.oneAgoBeanNum, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.todayBeanNum, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.title, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.guoquHb, Color.parseColor("#555555"))
            remoteViews.setTextColor(R.id.hongbao, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.hongbao, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.tips, Color.parseColor("#555555"))
            remoteViews.setTextColor(R.id.updateTime, Color.parseColor("#555555"))
        }

        val hideDivider = CacheUtil.getString("hideDivider")
        if ("1" == hideDivider) {
            remoteViews.setViewVisibility(R.id.divider, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.divider, View.VISIBLE)
        }

        val cleatInt2 = Intent(MyApplication.mInstance, MainActivity::class.java)
        cleatInt2.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val clearIntent2 = PendingIntent.getActivity(MyApplication.mInstance, 3, cleatInt2, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.rightContent, clearIntent2)

        remoteViews.setTextViewText(R.id.beanNum, userBean.beanNum)
        remoteViews.setTextViewText(R.id.todayBean, "+" + userBean.todayBean)
        remoteViews.setTextViewText(R.id.todayBeanNum, userBean.todayBean.toString())
        remoteViews.setTextViewText(R.id.oneAgoBeanNum, userBean.ago1Bean.toString())
        remoteViews.setTextViewText(R.id.updateTime, "数据更新于:" + getCurrentData())
        remoteViews.setTextViewText(R.id.hongbao, userBean.hb)
        try {
            if (getCurrentHH() + userBean.countdownTime > 24) {
                remoteViews.setTextViewText(R.id.guoquHb, "明日过期:" + userBean.gqhb)
            } else {
                remoteViews.setTextViewText(R.id.guoquHb, "今日过期:" + userBean.gqhb)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            remoteViews.setTextViewText(R.id.guoquHb, "今日过期:" + userBean.gqhb)
        }
        remoteViews.setTextViewText(R.id.jingXiang, userBean.jxiang)

        val cleatIntent = Intent()
        cleatIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        cleatIntent.action = when (thisKey) {
            "ck" -> {
                "com.scott.sayhi"
            }
            "ck1" -> {
                "com.scott.sayhi1"
            }
            "ck2" -> {
                "com.scott.sayhi2"
            }
            "ck3" -> {
                "com.scott.sayhi3"
            }
            "ck4" -> {
                "com.scott.sayhi4"
            }
            "ck5" -> {
                "com.scott.sayhi5"
            }
            else -> {
                ""
            }
        }
        val clearIntent3 = PendingIntent.getBroadcast(MyApplication.mInstance, 0, cleatIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        remoteViews.setOnClickPendingIntent(R.id.headImg, clearIntent3)

        if (TextUtils.isEmpty(userBean.headImageUrl)) {
            Glide.with(MyApplication.mInstance)
                .load(R.mipmap.icon_head_def)
                .into(object : SimpleTarget<Drawable?>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                        val head = BitmapUtil.drawableToBitmap(resource)
                        remoteViews.setImageViewBitmap(R.id.headImg, BitmapUtil.createCircleBitmap(head))
                        pullWidget()
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        pullWidget()
                    }
                })
        } else {
            Glide.with(MyApplication.mInstance)
                .load(userBean.headImageUrl)
                .into(object : SimpleTarget<Drawable?>() {
                    override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable?>?) {
                        val head = BitmapUtil.drawableToBitmap(resource)
                        remoteViews.setImageViewBitmap(R.id.headImg, BitmapUtil.createCircleBitmap(head))
                        pullWidget()
                    }

                    override fun onLoadFailed(errorDrawable: Drawable?) {
                        pullWidget()
                    }
                })
        }
    }

    private fun pullWidget() {
        val manager = AppWidgetManager.getInstance(MyApplication.mInstance)
        val componentName = if ("ck" == thisKey) {
            ComponentName(MyApplication.mInstance, MyAppWidgetProvider::class.java)
        } else if ("ck1" == thisKey) {
            ComponentName(MyApplication.mInstance, MyAppWidgetProvider1::class.java)
        } else {
            ComponentName(MyApplication.mInstance, MyAppWidgetProvider2::class.java)
        }
        manager.updateAppWidget(componentName, remoteViews)
    }
}