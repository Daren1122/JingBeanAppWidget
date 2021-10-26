package com.wj.jd.activity

import com.wj.jd.BaseActivity
import com.wj.jd.R

class AboutActivity : BaseActivity() {

    override fun setLayoutId(): Int {
        return R.layout.activity_about
    }

    override fun initView() {
        setTitle("使用说明")
    }

    override fun initData() {
    }

    override fun setEvent() {
    }
}