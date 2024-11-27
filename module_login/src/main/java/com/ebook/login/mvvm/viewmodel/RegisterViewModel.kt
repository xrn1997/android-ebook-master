package com.ebook.login.mvvm.viewmodel

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableField
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
    @JvmField
    val username: ObservableField<String> = ObservableField()

    @JvmField
    val firstPwd: ObservableField<String> = ObservableField()

    @JvmField
    val secondPwd: ObservableField<String> = ObservableField()
    private var mVoidSingleLiveEvent: SingleLiveEvent<Void>? = null

    fun register() {
        if (TextUtils.isEmpty(username.get())) { //用户名为空
            showShort(getApplication<Application>().applicationContext, "用户名不能为空")
            return
        }
        if (TextUtils.getTrimmedLength(username.get()) < 11) { // 手机号码不足11位
            showShort(getApplication<Application>().applicationContext, "请输入正确的手机号")
            return
        }
        if (TextUtils.isEmpty(firstPwd.get()) || TextUtils.isEmpty((secondPwd.get()))) {
            showShort(getApplication<Application>().applicationContext, "密码未填写完整")
            return
        }
        if (!TextUtils.equals(firstPwd.get(), secondPwd.get())) { //两次密码不一致
            showShort(getApplication<Application>().applicationContext, "两次密码不一致")
            return
        }
        val username = username.get()
        val password = firstPwd.get()
        if (username == null || password == null) {
            Log.e(TAG, "modify: 用户名或密码为null")
            return
        }
        mModel.register(username, password)
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
