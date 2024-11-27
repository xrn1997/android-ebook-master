package com.ebook.me

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.blankj.utilcode.util.SPUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ebook.api.config.API
import com.ebook.common.event.KeyCode
import com.ebook.common.event.RxBusTag
import com.ebook.common.view.profilePhoto.CircleImageView
import com.ebook.common.view.profilePhoto.PhotoCutDialog.Companion.newInstance
import com.ebook.common.view.profilePhoto.PhotoCutDialog.OnPhotoClickListener
import com.ebook.me.databinding.ActivityModifyInformationBinding
import com.ebook.me.mvvm.factory.MeViewModelFactory
import com.ebook.me.mvvm.viewmodel.ModifyViewModel
import com.hwangjr.rxbus.RxBus
import com.hwangjr.rxbus.annotation.Subscribe
import com.hwangjr.rxbus.annotation.Tag
import com.hwangjr.rxbus.thread.EventThread
import com.therouter.TheRouter.build
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmActivity
import com.xrn1997.common.util.FileUtil.getRealPathFromUri


@Route(path = KeyCode.Me.MODIFY_PATH, params = ["needLogin", "true"])
class ModifyInformationActivity :
    BaseMvvmActivity<ActivityModifyInformationBinding, ModifyViewModel>() {
    private lateinit var imageView: CircleImageView

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.get().register(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
    }

    override fun onBindLayout(): Int {
        return R.layout.activity_modify_information
    }

    override fun onBindViewModel(): Class<ModifyViewModel> {
        return ModifyViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return MeViewModelFactory
    }

    override fun initViewObservable() {
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun initView() {
        imageView = binding.viewProfilePhoto
        binding.viewModifyProfilePhoto.setOnClickSettingBarViewListener { uploadHeadImage() }
        binding.viewModifyPwd.setOnClickSettingBarViewListener {
                build(KeyCode.Login.MODIFY_PATH).navigation()
        }
        binding.viewModifyNickname.setOnClickSettingBarViewListener {
            startActivity(
                Intent(
                    this@ModifyInformationActivity,
                    ModifyNicknameActivity::class.java
                )
            )
            }
    }

    override fun initData() {
        val url = SPUtils.getInstance().getString(KeyCode.Login.SP_IMAGE)
        Glide.with(this)
            .load(API.URL_HOST_USER + "user/image/" + url)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .fitCenter()
            .dontAnimate()
            .placeholder(ContextCompat.getDrawable(this, R.drawable.image_default))
            .into(imageView)
    }

    /**
     * 上传头像
     */
    private fun uploadHeadImage() {
        val photoCutDialog = newInstance()
        photoCutDialog.setOnClickListener(object : OnPhotoClickListener {
            override fun onScreenPhotoClick(uri: Uri?) {
                if (uri == null) {
                    Log.e(TAG, "uploadHeadImage: 图片uri为空")
                    return
                }
                val cropImagePath = getRealPathFromUri(
                    applicationContext, uri
                )
                if (cropImagePath != null) {
                    mViewModel.modifyProfilePhoto(cropImagePath)
                }
            }
        })
        photoCutDialog.show(supportFragmentManager, "photoDialog")
    }

    @Subscribe(thread = EventThread.MAIN_THREAD, tags = [Tag(RxBusTag.MODIFY_PROFILE_PICTURE)])
    fun setProfilePicture(path: String) {
        Glide.with(this@ModifyInformationActivity)
            .load(API.URL_HOST_USER + "user/image/" + path)
            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
            .fitCenter()
            .dontAnimate()
            .placeholder(
                ContextCompat.getDrawable(
                    this@ModifyInformationActivity,
                    R.drawable.image_default
                )
            )
            .into(imageView)
    }

    companion object {
        private val TAG: String = ModifyInformationActivity::class.java.simpleName
    }
}
