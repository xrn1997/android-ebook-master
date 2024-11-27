package debug

import android.content.Intent
import com.ebook.book.service.DownloadService
import com.xrn1997.common.BaseApplication

class BookApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        startService(Intent(this, DownloadService::class.java))
    }
}
