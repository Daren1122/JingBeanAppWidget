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
import java.util.HashMap
import kotlin.math.max

/**
 * author wangjing
 * Date 2021/10/13
 * Description
 */
class WidgetUpdateDataUtil {
    private lateinit var remoteViews: RemoteViews
    private var gson = Gson()
    private var todayTime: Long = 0
    private var ago4Time: Long = 0
    lateinit var thisKey: String
    private var userBean = UserBean()
    private val keyList = HashMap<String, Int>()
    private lateinit var key1Ago: String
    private lateinit var key2Ago: String
    private lateinit var key3Ago: String
    private lateinit var key4Ago: String

    @Synchronized
    fun updateWidget(key: String) {
        thisKey = key
        val str = HttpUtil.getCK(thisKey)
        if (TextUtils.isEmpty(str)) return

        if (TimeUtil.isFastClick()) {
            Log.i("====", "isFastClick")
            return
        }

        userBean.todayBean = 0;
        userBean.page = 1
        userBean.ago1Bean = 0;

        todayTime = TimeUtil.getTodayMillis(0)
        ago4Time = TimeUtil.getTodayMillis(-4)

        key1Ago = TimeUtil.getYesterDay(-1) + thisKey
        key2Ago = TimeUtil.getYesterDay(-2) + thisKey
        key3Ago = TimeUtil.getYesterDay(-3) + thisKey
        key4Ago = TimeUtil.getYesterDay(-4) + thisKey

        keyList.clear()

        remoteViews = RemoteViews(MyApplication.mInstance.packageName, R.layout.widges_layout)
        remoteViews.setViewPadding(R.id.rootParent, R.dimen.dp_15.dmToPx(), 0, R.dimen.dp_15.dmToPx(), 0)
        pullWidget()

        checkUpdate()

        getUserInfo()

        getUserInfo1()

        //在这里就进行判断 是否需要取全部数据 还是只取今天的数据
        val ago1 = CacheUtil.getString(key1Ago)
        val ago2 = CacheUtil.getString(key2Ago)
        val ago3 = CacheUtil.getString(key3Ago)
        val ago4 = CacheUtil.getString(key4Ago)
        if (TextUtils.isEmpty(ago1) || TextUtils.isEmpty(ago2) || TextUtils.isEmpty(ago3) || TextUtils.isEmpty(ago4)) {
            Log.i("====", "没有前几天缓存数据")
            getJingBeanData(false)
        } else {
            Log.i("====", "有前几天缓存数据")
            getJingBeanData(false)
        }

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
                    userBean.beanNum = job.optJSONObject("user").optString("jingBean")
                    userBean.nickName = job.optJSONObject("user").optString("petName")
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
    private fun getJingBeanData(isToday: Boolean) {
        Log.i("====page", userBean.page.toString())
        HttpUtil.getJD(thisKey, userBean.page, object : StringCallBack {
            override fun onSuccess(result: String) {
                Log.i("====", result)
                try {
                    val jingDouBean = gson.fromJson(result, JingDouBean::class.java)
                    val dataList = jingDouBean.detailList

                    var isFinish = false
                    for (i in dataList.indices) {
                        val detail = dataList[i]
                        val beanDay = parseTime(detail.date)!!
                        val dayName = detail.date.split(" ")[0] + thisKey
                        if (keyList[dayName] == null) {
                            if (detail.amount > 0 && !detail.eventMassage.contains("退还")) {
                                keyList[dayName] = detail.amount
                            }
                        } else {
                            if (detail.amount > 0 && !detail.eventMassage.contains("退还")) {
                                keyList[dayName] = keyList[dayName]!! + detail.amount
                            }
                        }

                        //取前四天缓存 如果存在前四天缓存 则只用取最新的数据 否则还是要请求网络
                        isFinish = if (isToday) {
                            beanDay < todayTime
                        } else {
                            beanDay < ago4Time
                        }
                    }

                    if (isFinish) {
                        if (isToday) {

                        } else {
                            if (keyList[key1Ago] == null) {
                                CacheUtil.putString(key1Ago, "0")
                            } else {
                                CacheUtil.putString(key1Ago, keyList[key1Ago].toString())
                            }

                            if (keyList[key2Ago] == null) {
                                CacheUtil.putString(key2Ago, "0")
                            } else {
                                CacheUtil.putString(key2Ago, keyList[key2Ago].toString())
                            }

                            if (keyList[key3Ago] == null) {
                                CacheUtil.putString(key3Ago, "0")
                            } else {
                                CacheUtil.putString(key3Ago, keyList[key3Ago].toString())
                            }

                            if (keyList[key4Ago] == null) {
                                CacheUtil.putString(key4Ago, "0")
                            } else {
                                CacheUtil.putString(key4Ago, keyList[key4Ago].toString())
                            }
                        }

                        userBean.todayBean = keyList[TimeUtil.getYesterDay(0) + thisKey]!!
                        userBean.ago1Bean = CacheUtil.getString(key1Ago)!!.toInt()
                        userBean.ago2Bean = CacheUtil.getString(key2Ago)!!.toInt()
                        userBean.ago3Bean = CacheUtil.getString(key3Ago)!!.toInt()
                        userBean.ago4Bean = CacheUtil.getString(key4Ago)!!.toInt()
                        setData()
                    } else {
                        userBean.page++
                        getJingBeanData(isToday)
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
            remoteViews.setViewVisibility(R.id.botParent, View.GONE)
        } else {
            remoteViews.setViewVisibility(R.id.botParent, View.VISIBLE)
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
            remoteViews.setTextColor(R.id.yesterDayTip, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.todayTip, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.oneAgoBeanNum, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.todayBeanNum, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.title, ColorUtil.transColor("#FF000000"))
            remoteViews.setTextColor(R.id.guoquHb, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.hongbao, ColorUtil.transColor("#FF0000"))
            remoteViews.setTextColor(R.id.hongbao, ColorUtil.transColor("#FF0000"))
            remoteViews.setTextColor(R.id.tips, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.updateTime, ColorUtil.transColor("#333333"))

            remoteViews.setTextColor(R.id.zhuTv1, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv2, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv3, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv4, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv5, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.day1, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.day2, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.day3, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.day4, ColorUtil.transColor("#333333"))
            remoteViews.setTextColor(R.id.day5, ColorUtil.transColor("#333333"))
        } else {
            remoteViews.setTextColor(R.id.nickName, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.jingXiang, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.beanNum, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.todayBean, Color.parseColor("#008000"))
            remoteViews.setTextColor(R.id.yesterDayTip, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.todayTip, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.oneAgoBeanNum, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.todayBeanNum, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.title, Color.parseColor("#FF000000"))
            remoteViews.setTextColor(R.id.guoquHb, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.hongbao, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.hongbao, Color.parseColor("#FF0000"))
            remoteViews.setTextColor(R.id.tips, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.updateTime, Color.parseColor("#333333"))

            remoteViews.setTextColor(R.id.zhuTv1, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv2, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv3, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv4, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.zhuTv5, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.day1, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.day2, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.day3, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.day4, Color.parseColor("#333333"))
            remoteViews.setTextColor(R.id.day5, Color.parseColor("#333333"))
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
        remoteViews.setTextViewText(R.id.updateTime, "数据更新于:" + getCurrentData())
        remoteViews.setTextViewText(R.id.hongbao, userBean.hb)

        val showType = CacheUtil.getString("douShowType")
        Log.i("====showType", showType.toString())
        if (TextUtils.isEmpty(showType) || "文字展示" == showType) {
            remoteViews.setViewVisibility(R.id.normalShow, View.VISIBLE)
            remoteViews.setViewVisibility(R.id.zhuShow, View.GONE)

            remoteViews.setTextViewText(R.id.todayBeanNum, userBean.todayBean.toString())
            remoteViews.setTextViewText(R.id.oneAgoBeanNum, userBean.ago1Bean.toString())
        } else if ("柱状图展示" == showType) {
            remoteViews.setViewVisibility(R.id.normalShow, View.GONE)
            remoteViews.setViewVisibility(R.id.zhuShow, View.VISIBLE)

            remoteViews.setInt(R.id.zhu1, "setBackgroundColor", Color.rgb((0..255).random(), (0..255).random(), (0..255).random()));
            remoteViews.setInt(R.id.zhu2, "setBackgroundColor", Color.rgb((0..255).random(), (0..255).random(), (0..255).random()));
            remoteViews.setInt(R.id.zhu3, "setBackgroundColor", Color.rgb((0..255).random(), (0..255).random(), (0..255).random()));
            remoteViews.setInt(R.id.zhu4, "setBackgroundColor", Color.rgb((0..255).random(), (0..255).random(), (0..255).random()));
            remoteViews.setInt(R.id.zhu5, "setBackgroundColor", Color.rgb((0..255).random(), (0..255).random(), (0..255).random()));

            remoteViews.setTextViewText(R.id.day1, TimeUtil.getYesterOnlyDay(-4))
            remoteViews.setTextViewText(R.id.day2, TimeUtil.getYesterOnlyDay(-3))
            remoteViews.setTextViewText(R.id.day3, TimeUtil.getYesterOnlyDay(-2))
            remoteViews.setTextViewText(R.id.day4, TimeUtil.getYesterOnlyDay(-1))
            remoteViews.setTextViewText(R.id.day5, TimeUtil.getYesterOnlyDay(0))

            remoteViews.setTextViewText(R.id.zhuTv1, userBean.ago4Bean.toString())
            remoteViews.setTextViewText(R.id.zhuTv2, userBean.ago3Bean.toString())
            remoteViews.setTextViewText(R.id.zhuTv3, userBean.ago2Bean.toString())
            remoteViews.setTextViewText(R.id.zhuTv4, userBean.ago1Bean.toString())
            remoteViews.setTextViewText(R.id.zhuTv5, userBean.todayBean.toString())

            val maxLength = R.dimen.dp_42.dmToPx()
            var maxDou = max(userBean.ago4Bean, userBean.ago3Bean)
            maxDou = max(maxDou, userBean.ago2Bean)
            maxDou = max(maxDou, userBean.ago1Bean)
            maxDou = max(maxDou, userBean.todayBean)
            Log.i("====maxDou", maxDou.toString())
            val padding1 = maxLength - maxLength * userBean.ago4Bean / maxDou
            val padding2 = maxLength - maxLength * userBean.ago3Bean / maxDou
            val padding3 = maxLength - maxLength * userBean.ago2Bean / maxDou
            val padding4 = maxLength - maxLength * userBean.ago1Bean / maxDou
            val padding5 = maxLength - maxLength * userBean.todayBean / maxDou

            remoteViews.setViewPadding(R.id.zhuTv1, 0, padding1, 0, 0)
            remoteViews.setViewPadding(R.id.zhuTv2, 0, padding2, 0, 0)
            remoteViews.setViewPadding(R.id.zhuTv3, 0, padding3, 0, 0)
            remoteViews.setViewPadding(R.id.zhuTv4, 0, padding4, 0, 0)
            remoteViews.setViewPadding(R.id.zhuTv5, 0, padding5, 0, 0)
        }

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
        val componentName = when (thisKey) {
            "ck" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider::class.java)
            }
            "ck1" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider1::class.java)
            }
            "ck2" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider2::class.java)
            }
            "ck3" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider3::class.java)
            }
            "ck4" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider4::class.java)
            }
            "ck5" -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider5::class.java)
            }
            else -> {
                ComponentName(MyApplication.mInstance, MyAppWidgetProvider5::class.java)
            }
        }
        manager.updateAppWidget(componentName, remoteViews)
    }
}