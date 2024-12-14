package com.ebook.login.mvvm.viewmodel

import android.app.Application
import android.text.TextUtils
import android.util.Log
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.LoginDTO
import com.ebook.login.mvvm.model.RegisterModel
import com.xrn1997.common.event.SingleLiveEvent
import com.xrn1997.common.http.ExceptionHandler
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel
import com.xrn1997.common.util.ToastUtil.showShort
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class RegisterViewModel(application: Application, model: RegisterModel) :
    BaseViewModel<RegisterModel>(application, model) {
    private var mVoidSingleLiveEvent: SingleLiveEvent<Void>? = null

    fun register(username: String, firstPwd: String, secondPwd: String) {
        if (TextUtils.isEmpty(username)) { //用户名为空
            showShort(getApplication<Application>().applicationContext, "用户名不能为空")
            return
        }
        if (TextUtils.getTrimmedLength(username) < 11) { // 手机号码不足11位
            showShort(getApplication<Application>().applicationContext, "请输入正确的手机号")
            return
        }
        if (TextUtils.isEmpty(firstPwd) || TextUtils.isEmpty((secondPwd))) {
            showShort(getApplication<Application>().applicationContext, "密码未填写完整")
            return
        }
        if (!TextUtils.equals(firstPwd, secondPwd)) { //两次密码不一致
            showShort(getApplication<Application>().applicationContext, "两次密码不一致")
            return
        }
        mModel.register(username, firstPwd)
            .doOnSubscribe(this)
            .subscribe(object : Observer<RespDTO<LoginDTO>> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onNext(loginDTORespDTO: RespDTO<LoginDTO>) {
                    if (loginDTORespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                        showShort(getApplication<Application>().applicationContext, "注册成功")
                        postFinishActivityEvent()
                    } else {
                        Log.v(TAG, "error:" + loginDTORespDTO.error)
                    }
                }

                override fun onError(e: Throwable) {
                    getMVoidSingleLiveEvent().call()
                }

                override fun onComplete() {
                    getMVoidSingleLiveEvent().call()
                }
            })
    }

    private fun getMVoidSingleLiveEvent(): SingleLiveEvent<Void> {
        return createLiveData(mVoidSingleLiveEvent).also { mVoidSingleLiveEvent = it }
    }

    companion object {
        private val TAG: String = RegisterViewModel::class.java.simpleName
    }
}
