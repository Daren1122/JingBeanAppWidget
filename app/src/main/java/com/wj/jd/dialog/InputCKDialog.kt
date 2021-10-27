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
        setContentView(R.layout.dialog_layout_style4)
        input = findViewById(R.id.inputColor)

        ok = findViewById(R.id.ok)
        val divierId = context.resources.getIdentifier("android:id/titleDivider", null, null)
        val divider = findViewById<View>(divierId)
        divider?.setBackgroundColor(Color.TRANSPARENT)
        window?.setBackgroundDrawableResource(android.R.color.transparent)
        window?.setLayout(context.resources.displayMetrics.widthPixels * 5 / 6, LinearLayout.LayoutParams.WRAP_CONTENT)
        ok.setOnClickListener {
            if(TextUtils.isEmpty(input.text.toString())){
                Toast.makeText(MyApplication.mInstance, "CK为空", Toast.LENGTH_SHORT).show()
            }else{
                onOkClickListener?.ok(input.text.toString())
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