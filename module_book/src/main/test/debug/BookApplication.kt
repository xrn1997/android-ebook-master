package debug

import android.content.Intent
import com.ebook.book.service.DownloadService
import com.ebook.common.event.KeyCode
import com.ebook.db.ObjectBoxManager
import com.therouter.router.addPathReplaceInterceptor
import com.therouter.router.interceptor.PathReplaceInterceptor
import com.xrn1997.common.BaseApplication

class BookApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ObjectBoxManager.init(context)
        startService(Intent(this, DownloadService::class.java))
        addPathReplaceInterceptor(object : PathReplaceInterceptor() {
            override fun replace(path: String?): String? {
                if (path == KeyCode.Login.LOGIN_PATH) {
                    return KeyCode.Book.TEST_LOGIN_PATH
                }
                return path
            }
        })
    }
}
