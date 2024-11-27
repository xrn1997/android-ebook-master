package com.ebook.login.mvvm.viewmodel

import android.app.Application
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.databinding.ObservableField
import com.blankj.utilcode.util.SPUtils
import com.ebook.api.RetrofitManager
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.LoginDTO
import com.ebook.api.entity.User
import com.ebook.common.event.KeyCode
import com.ebook.common.event.RxBusTag
import com.ebook.login.mvvm.model.LoginModel
import com.hwangjr.rxbus.RxBus
import com.therouter.TheRouter.build
import com.xrn1997.common.http.ExceptionHandler
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel
import com.xrn1997.common.util.ToastUtil.showShort
import io.reactivex.rxjava3.core.Observer
import io.reactivex.rxjava3.disposables.Disposable

class LoginViewModel(application: Application, model: LoginModel) :
    BaseViewModel<LoginModel>(application, model) {
    @JvmField
    val username: ObservableField<String> = ObservableField()

    @JvmField
    val password: ObservableField<String> = ObservableField()
    var path: String? = null //被拦截的路径
    var bundle: Bundle? = null //被拦截的信息

    @JvmOverloads
    fun login(username: String? = this.username.get(), password: String? = this.password.get()) {
        if (username == null || password == null) {
            return
        }
        if (TextUtils.isEmpty(username)) { //用户名为空
            showShort(getApplication<Application>().applicationContext, "用户名不能为空")
            //  Log.d(TAG, "login: " + username);
            return
        }
        if (username.length < 11) { // 手机号码不足11位
            showShort(getApplication<Application>().applicationContext, "请输入正确的手机号")
            return
        }
        if (TextUtils.isEmpty(password)) { //密码为空
            showShort(getApplication<Application>().applicationContext, "密码不能为空")
            return
        }

        LoginModel.login(username, password).subscribe(object : Observer<RespDTO<LoginDTO>> {
            override fun onSubscribe(d: Disposable) {
                postShowLoadingViewEvent(true)
            }

            override fun onNext(loginDTORespDTO: RespDTO<LoginDTO>) {
                when (loginDTORespDTO.code) {
                    ExceptionHandler.AppError.SUCCESS -> {
                        RetrofitManager.getInstance().TOKEN =
                            "Bearer " + loginDTORespDTO.data?.token
                        val user = loginDTORespDTO.data?.user
                        if (user == null) {
                            Log.e(TAG, "onNext: user==null")
                            return
                        }
                        user.password = password //返回的是加密过的密码，不能使用，需要记住本地输入的密码。
                        loginOnNext(user) //非自动登录
                        RxBus.get().post(RxBusTag.SET_PROFILE_PICTURE_AND_NICKNAME, Any()) //通知其更新UI
                    }

                    ExceptionHandler.SystemError.UNAUTHORIZED -> {
                        RxBus.get().post(RxBusTag.SET_PROFILE_PICTURE_AND_NICKNAME, Any())
                        SPUtils.getInstance().clear()
                        //   Log.d(TAG, "登录失效 is login 状态：" + SPUtils.getInstance().getString(KeyCode.Login.SP_IS_LOGIN));
                        Log.v(TAG, "error:" + loginDTORespDTO.error)
                    }

                    else -> {
                        Log.v(TAG, "error:" + loginDTORespDTO.error)
                    }
                }
            }

            override fun onError(e: Throwable) {
                postShowLoadingViewEvent(false)
            }

            override fun onComplete() {
                postShowLoadingViewEvent(false)
            }
        })
    }

    private fun loginOnNext(user: User) {
        //不是自动登录则调用以下语句
        val spUtils = SPUtils.getInstance()
        if (!spUtils.getBoolean(KeyCode.Login.SP_IS_LOGIN)) {
            spUtils.put(KeyCode.Login.SP_IS_LOGIN, true)
            spUtils.put(KeyCode.Login.SP_USERNAME, user.username)
            spUtils.put(KeyCode.Login.SP_PASSWORD, user.password)
            spUtils.put(KeyCode.Login.SP_NICKNAME, user.nickname)
            spUtils.put(KeyCode.Login.SP_USER_ID, user.id)
            spUtils.put(KeyCode.Login.SP_IMAGE, user.image)
            postShowLoadingViewEvent(false)
            build(path).with(bundle).navigation()
            postFinishActivityEvent()
            showShort(getApplication<Application>().applicationContext, "登录成功")
        }
    }


    companion object {
        private val TAG: String = LoginViewModel::class.java.simpleName
    }
}