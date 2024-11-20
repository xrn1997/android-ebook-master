package debug;

import com.ebook.common.BaseApplication;
import com.ebook.common.interceptor.LoginInterceptor;
import com.therouter.router.NavigatorKt;

public class LoginApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        NavigatorKt.addRouterReplaceInterceptor(new LoginInterceptor());
    }
}
