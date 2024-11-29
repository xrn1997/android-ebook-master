package com.ebook.login.mvvm.viewmodel

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
    /**
     * 当前该变量仅修改密码使用，注册不用。
     */
    @JvmField
    var username: String? = null
    @JvmField
    var mVerifyCode: String? = null // 验证码
    private lateinit var mVoidSingleLiveEvent: SingleLiveEvent<Unit>

    fun verify(username: String, verifyCode: String) {
        if (TextUtils.isEmpty(username)) { //用户名为空
            showShort(getApplication<Application>().applicationContext, "手机号不能为空")
            return
        } else if (TextUtils.getTrimmedLength(username) < 11) { // 手机号码不足11位
            showShort(getApplication<Application>().applicationContext, "请输入正确的手机号")
            return
        }
        if (!TextUtils.equals(verifyCode, mVerifyCode)) {
            showShort(getApplication<Application>().applicationContext, "请输入正确的验证码")
            return
        }
        postFinishActivityEvent()
        toFgtPwdActivity()
    }

    private fun toFgtPwdActivity() {
        val bundle = Bundle()
        bundle.putString("username", username)
        Log.e(TAG, "toFgtPwdActivity: username:$username")
        postStartActivityEvent(ModifyPwdActivity::class.java, bundle)
    }

    fun modify(firstPwd: String, secondPwd: String) {
        if (firstPwd.isEmpty() || secondPwd.isEmpty()) {
            showShort(getApplication<Application>().applicationContext, "密码未填写完整")
            return
        }
        if (firstPwd != secondPwd) { //两次密码不一致
            showShort(getApplication<Application>().applicationContext, "两次密码不一致")
            return
        }
        Log.d(TAG, "modify: username: ${username},password: $firstPwd")
        val username = this.username
        if (username == null) {
            Log.e(TAG, "modify: 用户名为null")
            return
        }
        mModel.modifyPwd(username, firstPwd)
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
                        bundle.putString("password", firstPwd)
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
