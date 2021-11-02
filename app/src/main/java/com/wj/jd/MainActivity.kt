package com.wj.jd

import android.app.ProgressDialog
import android.content.BroadcastReceiver
import android.content.Context
import kotlinx.android.synthetic.main.activity_main.*
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import com.liulishuo.filedownloader.BaseDownloadTask
import com.liulishuo.filedownloader.FileDownloader
import com.wj.jd.activity.AboutActivity
import com.wj.jd.activity.MuchCkActivity
import com.wj.jd.activity.MyWebActivity
import com.wj.jd.activity.SettingActivity
import com.wj.jd.bean.SimpleFileDownloadListener
import com.wj.jd.bean.VersionBean
import com.wj.jd.dialog.InputCKDialog
import com.wj.jd.dialog.InputDialog
import com.wj.jd.dialog.NewStyleDialog
import com.wj.jd.dialog.NormalInputCKDialog
import com.wj.jd.util.*
import com.zhy.base.fileprovider.FileProvider7
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.include_title.*
import org.json.JSONObject
import java.io.File

class MainActivity : BaseActivity() {
    private lateinit var notificationUpdateReceiver: NotificationUpdateReceiver
    private lateinit var notificationUpdateReceiver1: NotificationUpdateReceiver1
    private lateinit var notificationUpdateReceiver2: NotificationUpdateReceiver2

    private lateinit var notificationUpdateReceiver3: NotificationUpdateReceiver3
    private lateinit var notificationUpdateReceiver4: NotificationUpdateReceiver4
    private lateinit var notificationUpdateReceiver5: NotificationUpdateReceiver5

    override fun setLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun initView() {
        setTitle("京豆")
        back?.visibility = View.GONE

        setRightTitle("使用说明")
    }

    override fun initData() {
        checkAppUpdate()
        initNotification()
        startUpdateService()
    }

    private fun startUpdateService() {
        /*
        * app进入重新启动更新数据后台服务
        * */
        if ("1" != CacheUtil.getString("startUpdateService")) {
            UpdateTask.updateAll()
        }
    }

    private fun initNotification() {
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.scott.sayhi")
        notificationUpdateReceiver = NotificationUpdateReceiver()
        registerReceiver(notificationUpdateReceiver, intentFilter)

        val intentFilter1 = IntentFilter()
        intentFilter1.addAction("com.scott.sayhi1")
        notificationUpdateReceiver1 = NotificationUpdateReceiver1()
        registerReceiver(notificationUpdateReceiver1, intentFilter1)

        val intentFilter2 = IntentFilter()
        intentFilter2.addAction("com.scott.sayhi2")
        notificationUpdateReceiver2 = NotificationUpdateReceiver2()
        registerReceiver(notificationUpdateReceiver2, intentFilter2)

        val intentFilter3 = IntentFilter()
        intentFilter3.addAction("com.scott.sayhi3")
        notificationUpdateReceiver3 = NotificationUpdateReceiver3()
        registerReceiver(notificationUpdateReceiver3, intentFilter3)

        val intentFilter4 = IntentFilter()
        intentFilter4.addAction("com.scott.sayhi4")
        notificationUpdateReceiver4 = NotificationUpdateReceiver4()
        registerReceiver(notificationUpdateReceiver4, intentFilter4)

        val intentFilter5 = IntentFilter()
        intentFilter5.addAction("com.scott.sayhi5")
        notificationUpdateReceiver5 = NotificationUpdateReceiver5()
        registerReceiver(notificationUpdateReceiver5, intentFilter5)
    }

    private fun checkAppUpdate() {
        HttpUtil.getAppVer(object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    var gson = Gson()
                    val versionBean = gson.fromJson(result, VersionBean::class.java)
                    if (DeviceUtil.getAppVersionName().equals(versionBean.release)) {
                        Toast.makeText(this@MainActivity, "当前已是最新版本", Toast.LENGTH_SHORT).show()
                    } else {
                        if ("1" == versionBean.isUpdate) {
                            createDialog("版本更新", versionBean.content, "更新", object : NewStyleDialog.OnRightClickListener {
                                override fun rightClick() {
                                    downLoadApk(versionBean.content_url)
                                }
                            })
                        } else {
                            createDialog("版本更新", versionBean.content, "取消", "更新", object : NewStyleDialog.OnLeftClickListener {
                                override fun leftClick() {
                                    disMissDialog()
                                }
                            }, object : NewStyleDialog.OnRightClickListener {
                                override fun rightClick() {
                                    disMissDialog()
                                    downLoadApk(versionBean.content_url)
                                }
                            })
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFail() {
            }

        })
    }

    //context.getExternalFilesDir()
    private fun downLoadApk(contentUrl: String?) {
        downLoad(contentUrl)
    }

    private lateinit var pd: ProgressDialog

    ///storage/emulated/0/Android/data/<包名>/files
    ///storage/emulated/0/Android/data/com.wj.jd/files
    private fun downLoad(contentUrl: String?) {
        if (TextUtils.isEmpty(contentUrl)) return

        pd = ProgressDialog(this)
        pd.setTitle("提示")
        pd.setMessage("软件版本更新中，请稍后...")
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL) //设置带进度条的

        pd.max = 100
        pd.setCancelable(false)
        pd.show()

        var pathParent = filesDir.path + "/downApk"
        var apkName = pathParent + System.currentTimeMillis() + ".apk"
        val file = File(pathParent)
        if (!file.exists()) {
            file.mkdirs()
        }

        FileDownloader.setup(this)
        FileDownloader.getImpl().create(contentUrl)
            .setPath(apkName)
            .setListener(object : SimpleFileDownloadListener() {
                override fun progress(task: BaseDownloadTask?, soFarBytes: Int, totalBytes: Int) {
                    val per = soFarBytes / (totalBytes / 100)
                    pd.progress = per
                }

                override fun completed(task: BaseDownloadTask) {
                    pd.dismiss()
                    val file = File(apkName)
                    installApk(file)
                }
            }).start()

    }

    private fun installApk(file: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        FileProvider7.setIntentDataAndType(this, intent, "application/vnd.android.package-archive", file, true)
        startActivity(intent)
    }

    override fun setEvent() {
        uploadCk.setOnClickListener {
            createDialog("提示", "是否已有京东CK", "没有京东CK", "已有京东CK", object : NewStyleDialog.OnLeftClickListener {
                override fun leftClick() {
                    hasNotCk()
                    disMissDialog()
                }
            }, object : NewStyleDialog.OnRightClickListener {
                override fun rightClick() {
                    haveCK()
                    disMissDialog()
                }
            })
        }

        setting.setOnClickListener {
            val intent = Intent(this, SettingActivity::class.java)
            startActivity(intent)
        }

        muchCK.setOnClickListener {
            val intent = Intent(this, MuchCkActivity::class.java)
            startActivity(intent)
        }

        loginJd.setOnClickListener {
            val intent = Intent(this, MyWebActivity::class.java)
            intent.putExtra("url", "https://plogin.m.jd.com/login/login")
            intent.putExtra("title", "京东网页版获取CK")
            startActivity(intent)
        }

        updateCK.setOnClickListener {
            val normalInputCKDialog = NormalInputCKDialog(this)
            normalInputCKDialog.onOkClickListener = object : NormalInputCKDialog.OnOkClickListener {
                override fun ok(ck: String, remark: String) {
                    if (TextUtils.isEmpty(ck)) {
                        Toast.makeText(this@MainActivity, "CK为空，添加失败", Toast.LENGTH_SHORT).show()
                    } else {
                        CacheUtil.putString("ck", ck)
                        Toast.makeText(this@MainActivity, "CK添加成功", Toast.LENGTH_SHORT).show()
                        UpdateTask.widgetUpdateDataUtil.updateWidget("ck")
                    }
                }
            }
            normalInputCKDialog.pop()
        }

        addQQGroup.setOnClickListener {
            joinQQGroup("qxW1vPr7rdC3o7W4Bes1xsh94xx4QOPV")
        }

        addTGGroup.setOnClickListener {
            val intent = Intent()
            intent.action = "android.intent.action.VIEW"
            intent.data = Uri.parse("https://t.me/joinchat/VJICOAj1z2BmOGVl")
            startActivity(intent)
        }

        rightTitle.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }
    }

    /*
    * 有京东CK
    * */
    private fun haveCK() {
        val inputCKDialog = InputCKDialog(this)
        inputCKDialog.onOkClickListener = object : InputCKDialog.OnOkClickListener {
            override fun ok(ck: String, remark: String) {
                checkCk(ck, remark)
            }
        }
        inputCKDialog.pop()
    }

    private fun checkCk(ck: String, remark: String) {
        if (TextUtils.isEmpty(remark)) {
            Toast.makeText(MyApplication.mInstance, "请输入备注！", Toast.LENGTH_SHORT).show()
            return
        }
        if (TextUtils.isEmpty(ck)) {
            Toast.makeText(MyApplication.mInstance, "请输入CK！", Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(MyApplication.mInstance, "正在检测CK有效性...", Toast.LENGTH_SHORT).show()
        HttpUtil.getUserInfoByCk(ck, object : StringCallBack {
            override fun onSuccess(result: String) {
                try {
                    val job = JSONObject(result)
                    var name = job.optJSONObject("data").optJSONObject("userInfo").optJSONObject("baseInfo").optString("nickname")
                    if (!TextUtils.isEmpty(name)) {
                        HttpUtil.sendCK(remark, ck, object : StringCallBack {
                            override fun onSuccess(result: String) {
                                Toast.makeText(MyApplication.mInstance, result, Toast.LENGTH_SHORT).show()
                            }

                            override fun onFail() {
                                Toast.makeText(MyApplication.mInstance, "连接错误！", Toast.LENGTH_SHORT).show()
                            }
                        })
                    } else {
                        Toast.makeText(MyApplication.mInstance, "CK有效性检测未通过，提交失败...", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    Toast.makeText(MyApplication.mInstance, "CK有效性检测未通过，提交失败...", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFail() {
                Toast.makeText(MyApplication.mInstance, "连接失败...", Toast.LENGTH_SHORT).show()
            }
        })
    }

    /*
    * 没有有京东CK
    * */
    private fun hasNotCk() {
        val intent = Intent(this, MyWebActivity::class.java)
        intent.putExtra("url", "https://plogin.m.jd.com/login/login")
        intent.putExtra("title", "京东网页版获取CK提交青龙")
        intent.putExtra("type", "1")
        startActivity(intent)
    }

    /****************
     *
     * 发起添加群流程。群号：豆豆交流群。(908891563) 的 key 为： n5xKKCpsHU-7IfmhYguyVmYXGo8t2pGy
     * 调用 joinQQGroup(n5xKKCpsHU-7IfmhYguyVmYXGo8t2pGy) 即可发起手Q客户端申请加群 豆豆交流群。(908891563)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回false表示呼起失败
     */
    fun joinQQGroup(key: String): Boolean {
        val intent = Intent()
        intent.data = Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3D$key")
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            startActivity(intent)
            true
        } catch (e: java.lang.Exception) {
            // 未安装手Q或安装的版本不支持
            false
        }
    }

    inner class NotificationUpdateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver")
            UpdateTask.widgetUpdateDataUtil.updateWidget("ck")
        }
    }

    inner class NotificationUpdateReceiver1 : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver1")
            UpdateTask.widgetUpdateDataUtil1.updateWidget("ck1")
        }
    }

    inner class NotificationUpdateReceiver2 : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver2")
            UpdateTask.widgetUpdateDataUtil2.updateWidget("ck2")
        }
    }

    inner class NotificationUpdateReceiver3 : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver3")
            UpdateTask.widgetUpdateDataUtil3.updateWidget("ck3")
        }
    }

    inner class NotificationUpdateReceiver4 : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver4")
            UpdateTask.widgetUpdateDataUtil4.updateWidget("ck4")
        }
    }

    inner class NotificationUpdateReceiver5 : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("====", "NotificationUpdateReceiver5")
            UpdateTask.widgetUpdateDataUtil5.updateWidget("ck5")
        }
    }
}