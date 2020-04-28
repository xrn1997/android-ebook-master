package com.ebook.login.mvvm.viewmodel;

import android.app.Application;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.SPUtils;
import com.ebook.api.RetrofitManager;
import com.ebook.api.dto.RespDTO;
import com.ebook.api.http.ExceptionHandler;
import com.ebook.api.user.LoginDTO;
import com.ebook.common.event.KeyCode;
import com.ebook.common.event.SingleLiveEvent;
import com.ebook.common.mvvm.viewmodel.BaseViewModel;
import com.ebook.common.util.ToastUtil;
import com.ebook.login.mvvm.model.LoginModel;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LoginViewModel extends BaseViewModel<LoginModel> {
    private static String TAG = LoginViewModel.class.getSimpleName();
    private SingleLiveEvent<Void> mVoidSingleLiveEvent;
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public String path;//被拦截的路径
    public Bundle bundle;//被拦截的信息

    public LoginViewModel(@NonNull Application application, LoginModel model) {
        super(application, model);
    }

    public void login() {
        //重载login()
        login(username.get(), password.get());
    }

    public void login(String username, String password) {
        if (TextUtils.isEmpty(username)) {//用户名为空
            ToastUtil.showToast("用户名不能为空");
            Log.d(TAG, "login: " + username);
            return;
        }
        if (username.length() < 11) { // 手机号码不足11位
            ToastUtil.showToast("请输入正确的手机号");
            return;
        }
        if (TextUtils.isEmpty(password)) {//密码为空
            ToastUtil.showToast("密码不能为空");
            return;
        }

        mModel.login(username, password).subscribe(new Observer<RespDTO<LoginDTO>>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RespDTO<LoginDTO> loginDTORespDTO) {
                Log.d(TAG, "onNext: start");
                if (loginDTORespDTO.code == ExceptionHandler.APP_ERROR.SUCC) {
                    Log.v(TAG, "tolen:" + loginDTORespDTO.data.getToken());
                    RetrofitManager.getInstance().TOKEN = "Bearer " + loginDTORespDTO.data.getToken();
                    loginOnNext(username, password);//非自动登录
                } else if (loginDTORespDTO.code == ExceptionHandler.SYSTEM_ERROR.UNAUTHORIZED) {
                    SPUtils.getInstance().clear();
                    Log.d(TAG, "登录失效 is login 状态：" + SPUtils.getInstance().getString(KeyCode.Login.SP_IS_LOGIN));
                    Log.v(TAG, "error:" + loginDTORespDTO.error);
                } else {
                    Log.v(TAG, "error:" + loginDTORespDTO.error);
                }
            }

            @Override
            public void onError(Throwable e) {
                getmVoidSingleLiveEvent().call();
            }

            @Override
            public void onComplete() {
                // Log.d(TAG, "onComplete: start");
                getmVoidSingleLiveEvent().call();
                //Log.d(TAG, "onComplete: end");
            }
        });
    }

    private void loginOnNext(String username, String password) {
        //不是自动登录则调用以下语句
        if (!SPUtils.getInstance().getBoolean(KeyCode.Login.SP_IS_LOGIN)) {
            SPUtils.getInstance().put(KeyCode.Login.SP_IS_LOGIN, true);
            SPUtils.getInstance().put(KeyCode.Login.SP_USERNAME, username);
            SPUtils.getInstance().put(KeyCode.Login.SP_PASSWORD, password);
            postShowTransLoadingViewEvent(false);
            toAimActivity();
            postFinishActivityEvent();
            ToastUtil.showToast("登录成功");
            Log.d(TAG, "onNext: finish");
        }
    }

    private void toAimActivity() {
        if (!TextUtils.isEmpty(path)) {
            ARouter router = ARouter.getInstance();
            if (!bundle.isEmpty()) {
                router.build(path).with(bundle).navigation();
            } else {
                router.build(path).navigation();
            }
        }
    }

    public SingleLiveEvent<Void> getmVoidSingleLiveEvent() {
        // Log.d(TAG, "getmVoidSingleLiveEvent: start");
        mVoidSingleLiveEvent = createLiveData(mVoidSingleLiveEvent);
        // Log.d(TAG, "getmVoidSingleLiveEvent: end");
        return mVoidSingleLiveEvent;
    }

}
