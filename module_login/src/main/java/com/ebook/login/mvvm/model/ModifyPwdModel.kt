package com.ebook.login.mvvm.model

import android.app.Application
import com.ebook.api.config.API
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.User
import com.ebook.api.service.UserService
import com.xrn1997.common.http.RxJavaAdapter.exceptionTransformer
import com.xrn1997.common.http.RxJavaAdapter.schedulersTransformer
import com.xrn1997.common.manager.RetrofitManager
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable

class ModifyPwdModel(application: Application) : BaseModel(application) {
    private val mUserService: UserService = RetrofitManager.create(UserService::class.java)

    init {
        // 通过反射动态修改 BaseUrl
        RetrofitManager.mHttpUrl.setHost(API.URL_HOST_USER)
        RetrofitManager.mHttpUrl.setPort(API.URL_PORT_USER)
    }
    fun modifyPwd(username: String, password: String): Observable<RespDTO<Int>> {
        val result = mUserService.modifyPwd(User(username, password))
        return result.compose(schedulersTransformer()).compose(exceptionTransformer())
    }
}
