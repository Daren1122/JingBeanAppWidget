package com.wj.jd.activity

import com.wj.jd.BaseActivity
import com.wj.jd.R
import kotlinx.android.synthetic.main.activity_about.*
import android.content.Intent




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
        aliPay.setOnClickListener {
            val urlCode = "fkx19583yrbamgyk13ghe49"
            val intent = Intent.parseUri("intent://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2F{urlCode}%3F_s%3Dweb-other&_t=1472443966571#Intent;scheme=alipayqr;package=com.eg.android.AlipayGphone;end".replace("{urlCode}", urlCode), 1)
            startActivity(intent);
        }
    }
}