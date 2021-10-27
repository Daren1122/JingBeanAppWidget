package com.wj.jd.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.view.View
import android.webkit.CookieManager
import android.webkit.CookieSyncManager
import android.widget.Toast
import com.wj.jd.BaseActivity
import com.wj.jd.MyApplication
import com.wj.jd.R
import com.wj.jd.dialog.NewStyleDialog
import com.wj.jd.util.HttpUtil
import com.wj.jd.util.StringCallBack
import com.wj.jd.webView.CommonWebView
import kotlinx.android.synthetic.main.activity_web.*

class MyWebActivity : BaseActivity() {
    private var type: String? = null

    override fun setLayoutId(): Int {
        return R.layout.activity_web
    }

    override fun initView() {
        type = intent.getStringExtra("type")
        val title = intent.getStringExtra("title")
        if (TextUtils.isEmpty(title)) {
            setTitle("网页浏览器")
        } else {
            setTitle(title.toString())
        }
        if("1" == type){
            tips.visibility = View.VISIBLE
        }else{
            tips.visibility = View.GONE
        }
    }

    override fun initData() {
        removeCookie(this)
        var url = intent.getStringExtra("url")
        mCommonWebView.loadUrl(url)
    }

    override fun setEvent() {
        mCommonWebView.setOnGetCookieListener(object : CommonWebView.OnGetCookie {
            override fun get(ck: String) {
                dealCk(ck)
            }
        })
    }

    private fun dealCk(ck: String) {
        if("1" == type){
            createDialog("已获取到CK,是否上传到青龙挂京豆？", ck, "取消", "上传", object : NewStyleDialog.OnLeftClickListener {
                override fun leftClick() {
                    disMissDialog()
                }
            }, object : NewStyleDialog.OnRightClickListener {
                override fun rightClick() {
                    disMissDialog()
                    sendCK(ck)
                }
            })
        }else{
            createDialog("已获取到CK", ck, "取消", "复制", object : NewStyleDialog.OnLeftClickListener {
                override fun leftClick() {
                    disMissDialog()
                }
            }, object : NewStyleDialog.OnRightClickListener {
                override fun rightClick() {
                    copyClipboard(ck)
                    disMissDialog()
                    finish()
                }
            })
        }
    }

    private fun sendCK(ck: String) {
        HttpUtil.sendCK("webView", ck, object : StringCallBack {
            override fun onSuccess(result: String) {
                Toast.makeText(MyApplication.mInstance, result, Toast.LENGTH_SHORT).show()
                finish()
            }

            override fun onFail() {
                Toast.makeText(MyApplication.mInstance, "连接错误！", Toast.LENGTH_SHORT).show()
            }
        })

    }

    private fun removeCookie(context: Context) {
        try {
            CookieSyncManager.createInstance(context)
            val cookieManager = CookieManager.getInstance()
            cookieManager.removeAllCookie()
            CookieSyncManager.getInstance().sync()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun copyClipboard(content: String?) {
        try {
            val myClipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val primaryClip = ClipData.newPlainText("text", content)
            myClipboard.setPrimaryClip(primaryClip)
            Toast.makeText(MyApplication.mInstance, "已复制CK到粘贴板", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}