package com.ebook.login.mvvm.viewmodel;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;

import com.ebook.api.dto.RespDTO;
import com.ebook.api.entity.LoginDTO;
import com.ebook.login.mvvm.model.RegisterModel;
import com.xrn1997.common.event.SingleLiveEvent;
import com.xrn1997.common.http.ExceptionHandler;
import com.xrn1997.common.mvvm.viewmodel.BaseViewModel;
import com.xrn1997.common.util.ToastUtil;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;


public class RegisterViewModel extends BaseViewModel<RegisterModel> {
    private static final String TAG = RegisterViewModel.class.getSimpleName();
    public final ObservableField<String> username = new ObservableField<>();
    public final ObservableField<String> password_1 = new ObservableField<>();
    public final ObservableField<String> password_2 = new ObservableField<>();
    private SingleLiveEvent<Void> mVoidSingleLiveEvent;

    public RegisterViewModel(@NonNull Application application, RegisterModel model) {
        super(application, model);
    }

    public void register() {

        if (TextUtils.isEmpty(username.get())) {//用户名为空
            ToastUtil.showShort(getApplication().getApplicationContext(), "用户名不能为空");
            return;
        }
        if (TextUtils.getTrimmedLength(username.get()) < 11) { // 手机号码不足11位
            ToastUtil.showShort(getApplication().getApplicationContext(), "请输入正确的手机号");
            return;
        }
        if (TextUtils.isEmpty(password_1.get()) || TextUtils.isEmpty((password_2.get()))) {
            ToastUtil.showShort(getApplication().getApplicationContext(), "密码未填写完整");
            return;
        }
        if (!TextUtils.equals(password_1.get(), password_2.get())) {//两次密码不一致
            ToastUtil.showShort(getApplication().getApplicationContext(), "两次密码不一致");
            return;
        }
        mModel.register(username.get(), password_1.get()).subscribe(new Observer<>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(RespDTO<LoginDTO> loginDTORespDTO) {
                if (loginDTORespDTO.code == ExceptionHandler.AppError.SUCCESS) {
                    ToastUtil.showShort(getApplication().getApplicationContext(), "注册成功");
                    postFinishActivityEvent();
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
                getmVoidSingleLiveEvent().call();
            }
        });
    }

    private SingleLiveEvent<Void> getmVoidSingleLiveEvent() {
        return mVoidSingleLiveEvent = createLiveData(mVoidSingleLiveEvent);
    }
}
