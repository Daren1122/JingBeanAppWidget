package com.wj.jd.activity

import android.text.TextUtils
import com.wj.jd.BaseActivity
import com.wj.jd.R
import android.widget.ImageView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.activity_widger_bac.*
import com.luck.picture.lib.entity.LocalMedia

import com.luck.picture.lib.listener.OnResultCallbackListener

import com.luck.picture.lib.config.PictureMimeType

import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.wj.jd.util.CacheUtil
import com.wj.jd.util.GlideEngine
import com.yalantis.ucrop.view.OverlayView


class WidgetBackSelActivity : BaseActivity() {

    override fun setLayoutId(): Int {
        return R.layout.activity_widger_bac
    }

    override fun initView() {
        setTitle("小组件背景图片设置")
    }

    override fun initData() {
        val cacheBac = CacheUtil.getString("ck_back")
        if (!TextUtils.isEmpty(cacheBac)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac).into(ckBac)
        }

        val cacheBac1 = CacheUtil.getString("ck1_back")
        if (!TextUtils.isEmpty(cacheBac1)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac1).into(ck1Bac)
        }

        val cacheBac2 = CacheUtil.getString("ck2_back")
        if (!TextUtils.isEmpty(cacheBac2)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac2).into(ck2Bac)
        }

        val cacheBac3 = CacheUtil.getString("ck3_back")
        if (!TextUtils.isEmpty(cacheBac3)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac3).into(ck3Bac)
        }

        val cacheBac4 = CacheUtil.getString("ck4_back")
        if (!TextUtils.isEmpty(cacheBac4)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac4).into(ck4Bac)
        }

        val cacheBac5 = CacheUtil.getString("ck5_back")
        if (!TextUtils.isEmpty(cacheBac5)) {
            Glide.with(this@WidgetBackSelActivity).load(cacheBac5).into(ck5Bac)
        }
    }

    override fun setEvent() {
        add.setOnClickListener {
            selectPicture("ck_back", ckBac)
        }

        del.setOnClickListener {
            CacheUtil.putString("ck_back", "")
            ckBac.setImageBitmap(null)
        }

        add1.setOnClickListener {
            selectPicture("ck1_back", ck1Bac)
        }

        del1.setOnClickListener {
            CacheUtil.putString("ck1_back", "")
            ck1Bac.setImageBitmap(null)
        }

        add2.setOnClickListener {
            selectPicture("ck2_back", ck2Bac)
        }

        del2.setOnClickListener {
            CacheUtil.putString("ck2_back", "")
            ck2Bac.setImageBitmap(null)
        }

        add3.setOnClickListener {
            selectPicture("ck3_back", ck3Bac)
        }

        del3.setOnClickListener {
            CacheUtil.putString("ck3_back", "")
            ck3Bac.setImageBitmap(null)
        }

        add4.setOnClickListener {
            selectPicture("ck4_back", ck4Bac)
        }

        del4.setOnClickListener {
            CacheUtil.putString("ck4_back", "")
            ck4Bac.setImageBitmap(null)
        }

        add5.setOnClickListener {
            selectPicture("ck5_back", ck5Bac)
        }

        del5.setOnClickListener {
            CacheUtil.putString("ck5_back", "")
            ck5Bac.setImageBitmap(null)
        }
    }

    private fun selectPicture(key: String, ck1Bac: ImageView) {
        PictureSelector.create(this)
            .openGallery(PictureMimeType.ofImage())
            .selectionMode(PictureConfig.SINGLE)
            .imageEngine(GlideEngine.createGlideEngine())
            .isAndroidQTransform(true)
            .isEnableCrop(true)
            .isCropDragSmoothToCenter(false)
            .freeStyleCropMode(OverlayView.FREESTYLE_CROP_MODE_ENABLE)
            .synOrAsy(true)
            .withAspectRatio(16, 9)
            .rotateEnabled(true)
            .forResult(object : OnResultCallbackListener<LocalMedia?> {
                override fun onResult(result: List<LocalMedia?>) {
                    // 结果回调
                    val localMedia = result[0] ?: return
                    val path = localMedia.cutPath
                    Glide.with(this@WidgetBackSelActivity).load(path).into(ck1Bac)
                    CacheUtil.putString(key, path)
                }

                override fun onCancel() {
                    // 取消
                }
            })

    }
}