package com.ebook.login.mvvm.model

import android.app.Application
import com.ebook.api.RetrofitManager
import com.ebook.api.dto.RespDTO
import com.ebook.api.entity.User
import com.ebook.api.service.UserService
import com.xrn1997.common.http.RxJavaAdapter.exceptionTransformer
import com.xrn1997.common.http.RxJavaAdapter.schedulersTransformer
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable

class ModifyPwdModel(application: Application) : BaseModel(application) {
    private val mUserService: UserService = RetrofitManager.getInstance().userService

    fun modifyPwd(username: String, password: String): Observable<RespDTO<Int>> {
        val result = mUserService.modifyPwd(User(username, password))
        return result.compose(schedulersTransformer()).compose(exceptionTransformer())
    }
}
