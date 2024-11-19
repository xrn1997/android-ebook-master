package debug

import com.ebook.common.BaseApplication
import com.ebook.common.interceptor.LoginInterceptor
import com.therouter.router.addRouterReplaceInterceptor

class MeApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        addRouterReplaceInterceptor(LoginInterceptor())
    }
}
