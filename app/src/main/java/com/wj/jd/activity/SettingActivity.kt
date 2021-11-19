package com.wj.jd.activity

import android.content.Intent
import android.text.TextUtils
import android.widget.Toast
import com.wj.jd.BaseActivity
import com.wj.jd.MyApplication
import com.wj.jd.R
import com.wj.jd.dialog.InputDialog
import com.wj.jd.dialog.MenuDialog
import com.wj.jd.util.CacheUtil
import com.wj.jd.util.UpdateTask
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : BaseActivity() {
    var paddingDataList = ArrayList<String>()
    var douShowTypeList = ArrayList<String>()

    override fun setLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun initView() {
        setTitle("小组件设置")

        paddingDataList.add("无边距")
        paddingDataList.add("5dp")
        paddingDataList.add("10dp")
        paddingDataList.add("15dp")
        paddingDataList.add("20dp")

        douShowTypeList.add("文字展示")
        douShowTypeList.add("柱状图展示")
    }

    override fun initData() {
        hideTips.isChecked = "1" == CacheUtil.getString("hideTips")

        hideNichen.isChecked = "1" == CacheUtil.getString("hideTips")

        colorSwitch.isChecked = "1" == CacheUtil.getString("colorSwitch")

        startUpdateService.isChecked = "1" != CacheUtil.getString("startUpdateService")

        hideDivider.isChecked = "1" == CacheUtil.getString("hideDivider")

        val paddingType = CacheUtil.getString("paddingType")
        paddingTip.text = if (TextUtils.isEmpty(paddingType)) {
            "15dp"
        } else {
            paddingType
        }

        val douShowTypeTip = CacheUtil.getString("douShowType")
        douShowType.text = if (TextUtils.isEmpty(douShowTypeTip)) {
            "文字展示"
        } else {
            douShowTypeTip
        }

        var designColorTxt = CacheUtil.getString("designColor")
        if (TextUtils.isEmpty(designColorTxt)) {
            designColorTxt = "#FFFFFF"
        }
        designColor.text = designColorTxt
    }

    override fun setEvent() {
        hideTips.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                CacheUtil.putString("hideTips", "1")
            } else {
                CacheUtil.putString("hideTips", "0")
            }
        }

        hideNichen.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                CacheUtil.putString("hideNichen", "1")
            } else {
                CacheUtil.putString("hideNichen", "0")
            }
        }

        startUpdateService.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                CacheUtil.putString("startUpdateService", "0")
            } else {
                CacheUtil.putString("startUpdateService", "1")
            }
        }

        colorSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                CacheUtil.putString("colorSwitch", "1")
            } else {
                CacheUtil.putString("colorSwitch", "0")
            }
        }

        hideDivider.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                CacheUtil.putString("hideDivider", "1")
            } else {
                CacheUtil.putString("hideDivider", "0")
            }
        }

        settingFinish.setOnClickListener {
            UpdateTask.updateAll()
            Toast.makeText(this, "小组件状态更新完毕", Toast.LENGTH_SHORT).show()
        }

        douShowType.setOnClickListener {
            var menuDialog = MenuDialog(this, douShowTypeList) {
                CacheUtil.putString("douShowType", it)
                douShowType.text = it
            }
            menuDialog.pop()
        }

        paddingTip.setOnClickListener {
            var menuDialog = MenuDialog(this, paddingDataList) {
                CacheUtil.putString("paddingType", it)
                paddingTip.text = it
            }
            menuDialog.pop()
        }

        designColor.setOnClickListener {
            var inputDialog = InputDialog(this)
            inputDialog.onOkClickListener = object : InputDialog.OnOkClickListener {
                override fun ok(str: String) {
                    CacheUtil.putString("designColor", str)
                    designColor.text = str
                }
            }
            inputDialog.pop()
        }

        goToSelect.setOnClickListener {
            var intent = Intent(this, WidgetBackSelActivity::class.java)
            startActivity(intent)
        }
    }
}