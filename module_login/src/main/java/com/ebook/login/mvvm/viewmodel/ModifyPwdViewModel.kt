package com.ebook.login.mvvm.viewmodel

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableField
import com.blankj.utilcode.util.SPUtils
import com.ebook.api.dto.RespDTO
import com.ebook.common.event.KeyCode
import com.ebook.common.event.RxBusTag
import com.ebook.login.ModifyPwdActivity
import com.ebook.login.mvvm.model.ModifyPwdModel
import com.hwangjr.rxbus.RxBus
import com.therouter.TheRouter.build
import com.xrn1997.common.event.SingleLiveEvent
import com.xrn1997.common.http.ExceptionHandler
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel
import com.xrn1997.common.util.ToastUtil.showShort
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable


class ModifyPwdViewModel(application: Application, model: ModifyPwdModel) :
    BaseViewModel<ModifyPwdModel>(application, model) {
    @JvmField
    val username: ObservableField<String> = ObservableField()

    @JvmField
    val verifyCode: ObservableField<String> = ObservableField()

    @JvmField
    val firstPwd: ObservableField<String> = ObservableField()

    @JvmField
    val secondPwd: ObservableField<String> = ObservableField()

    @JvmField
    var mVerifyCode: String? = null // 验证码
    private lateinit var mVoidSingleLiveEvent: SingleLiveEvent<Unit>


    fun verify() {
        if (TextUtils.isEmpty(username.get())) { //用户名为空
            showShort(getApplication<Application>().applicationContext, "手机号不能为空")
            return
        } else if (TextUtils.getTrimmedLength(username.get()) < 11) { // 手机号码不足11位
            showShort(getApplication<Application>().applicationContext, "请输入正确的手机号")
            return
        }
        if (!TextUtils.equals(verifyCode.get(), mVerifyCode)) {
            showShort(getApplication<Application>().applicationContext, "请输入正确的验证码")
            return
        }
        postFinishActivityEvent()
        toFgtPwdActivity()
    }

    private fun toFgtPwdActivity() {
        val bundle = Bundle()
        bundle.putString("username", username.get())
        Log.e(TAG, "toFgtPwdActivity: username:" + username.get())
        postStartActivityEvent(ModifyPwdActivity::class.java, bundle)
    }

    fun modify() {
        if (TextUtils.isEmpty(firstPwd.get()) || TextUtils.isEmpty((secondPwd.get()))) {
            showShort(getApplication<Application>().applicationContext, "密码未填写完整")
            return
        }
        if (!TextUtils.equals(firstPwd.get(), secondPwd.get())) { //两次密码不一致
            showShort(getApplication<Application>().applicationContext, "两次密码不一致")
            return
        }
        Log.d(TAG, "modify: username: ${username.get()},password: ${firstPwd.get()}")
        val username = username.get()
        val password = firstPwd.get()
        if (username == null || password == null) {
            Log.e(TAG, "modify: 用户名或密码为null")
            return
        }
        mModel.modifyPwd(username, password)
            .subscribe(object : Observer<RespDTO<Int>> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(loginDTORespDTO: RespDTO<Int>) {
                    //  Log.d(TAG, "修改密码onNext: start");
                    if (loginDTORespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                        showShort(getApplication<Application>().applicationContext, "修改成功")
                        SPUtils.getInstance().clear()
                        RxBus.get().post(RxBusTag.SET_PROFILE_PICTURE_AND_NICKNAME)
                        val bundle = Bundle()
                        bundle.putString("username", username)
                        bundle.putString("password", password)
                        build(KeyCode.Login.LOGIN_PATH)
                            .with(bundle)
                            .navigation()
                        //    Log.d(TAG, "修改密码onNext: finish");
                    } else {
                        Log.v(TAG, "修改密码error:" + loginDTORespDTO.error)
                    }
                }

                override fun onError(e: Throwable) {
                    getMVoidSingleLiveEvent().call()
                }

                override fun onComplete() {
                    //   Log.d(TAG, "修改密码onComplete: start");
                    getMVoidSingleLiveEvent().call()
                    postFinishActivityEvent()
                }
            })
    }

    private fun getMVoidSingleLiveEvent(): SingleLiveEvent<Unit> {
        // Log.d(TAG, "getMVoidSingleLiveEvent: start");
        mVoidSingleLiveEvent = createLiveData(mVoidSingleLiveEvent)
        // Log.d(TAG, "getMVoidSingleLiveEvent: end");
        return mVoidSingleLiveEvent
    }

    companion object {
        private val TAG: String = ModifyPwdViewModel::class.java.simpleName
    }
}