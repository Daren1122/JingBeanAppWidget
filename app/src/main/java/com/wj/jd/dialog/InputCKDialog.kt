package com.wj.jd.dialog

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wj.jd.MyApplication
import com.wj.jd.R
import com.wj.jd.util.CacheUtil

class InputCKDialog(var mActivity: Activity) : Dialog(mActivity!!) {
    private lateinit var input: EditText
    private lateinit var ok: Button
    var onOkClickListener: OnOkClickListener? = null

    init {
        initView()
    }

    fun initView(): InputCKDialog {
        setContentView(R.layout.dialog_layout_style3)
        input = findViewById(R.id.inputColor)

        var designColor = CacheUtil.getString("designColor")
        if(TextUtils.isEmpty(designColor)){
            designColor = "#FFFFFF"
        }
        input.setText(designColor)

        ok = findViewById(R.id.ok)
        val divierId = context.resources.getIdentifier("android:id/titleDivider", null, null)
        val divider = findViewById<View>(divierId)
        divider?.setBackgroundColor(Color.TRANSPARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(context.resources.displayMetrics.widthPixels * 5 / 6, LinearLayout.LayoutParams.WRAP_CONTENT)
        ok.setOnClickListener {
            try {
                Color.parseColor(input.text.toString())
                onOkClickListener?.ok(input.text.toString())
                dismiss()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(MyApplication.mInstance, "输入无效", Toast.LENGTH_SHORT).show()
            }
        }
        return this
    }

    fun pop() {
        if (!isShowing) {
            try {
                show()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun show() {
        try {
            super.show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    interface OnOkClickListener {
        fun ok(str: String)
    }
}