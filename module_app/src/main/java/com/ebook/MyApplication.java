package com.ebook;

import com.ebook.api.RetrofitManager;
import com.ebook.db.ObjectBoxManager;
import com.ebook.login.interceptor.LoginInterceptor;
import com.therouter.router.NavigatorKt;
import com.xrn1997.common.BaseApplication;


public class MyApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitManager.init(this);
        ObjectBoxManager.init(this);
        // 登录拦截
        NavigatorKt.addRouterReplaceInterceptor(new LoginInterceptor());
    }

}
