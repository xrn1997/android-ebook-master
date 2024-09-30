package com.ebook.login.mvvm.model;

import android.app.Application;

import com.ebook.api.RetrofitManager;
import com.ebook.api.dto.RespDTO;
import com.ebook.api.entity.LoginDTO;
import com.ebook.api.entity.User;
import com.ebook.api.service.UserService;
import com.xrn1997.common.http.RxJavaAdapter;
import com.xrn1997.common.mvvm.model.BaseModel;

import io.reactivex.rxjava3.core.Observable;

public class LoginModel extends BaseModel {
    private final UserService mUserService;

    public LoginModel(Application application) {
        super(application);
        mUserService = RetrofitManager.getInstance().getUserService();
    }

    public Observable<RespDTO<LoginDTO>> login(String username, String password) {
        Observable<RespDTO<LoginDTO>> result = mUserService.login(new User(username, password));
        return result
                .compose(RxJavaAdapter.INSTANCE.schedulersTransformer())
                .compose(RxJavaAdapter.INSTANCE.exceptionTransformer());
    }
}
