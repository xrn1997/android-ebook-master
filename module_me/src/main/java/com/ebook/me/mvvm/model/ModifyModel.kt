package com.ebook.me.mvvm.model

import android.app.Application
import com.blankj.utilcode.util.SPUtils
import com.ebook.api.RetrofitManager
import com.ebook.api.dto.RespDTO
import com.ebook.api.service.UserService
import com.ebook.common.event.KeyCode
import com.xrn1997.common.http.RxJavaAdapter.exceptionTransformer
import com.xrn1997.common.http.RxJavaAdapter.schedulersTransformer
import com.xrn1997.common.mvvm.model.BaseModel
import io.reactivex.rxjava3.core.Observable
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ModifyModel(application: Application) : BaseModel(application) {
    private val userService: UserService = RetrofitManager.getInstance().userService

    /**
     * 修改昵称
     */
    fun modifyNickname(nickname: String): Observable<RespDTO<Int>> {
        val username = SPUtils.getInstance().getString(KeyCode.Login.SP_USERNAME)
        return userService.modifyNickname(RetrofitManager.getInstance().TOKEN, username, nickname)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }

    /**
     * 修改头像
     *
     * @param path 头像路径
     * @return 返回服务器头像名称
     */
    fun modifyProfilePhoto(path: String): Observable<RespDTO<String>> {
        val username = SPUtils.getInstance().getString(KeyCode.Login.SP_USERNAME)
        val file = File(path)
        val requestBody: RequestBody =
            file.asRequestBody("application/octet-stream".toMediaTypeOrNull())
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.name, requestBody)

        return userService.modifyProfiePhoto(RetrofitManager.getInstance().TOKEN, username, body)
            .compose(schedulersTransformer())
            .compose(exceptionTransformer())
    }
}