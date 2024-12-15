package debug

import android.content.Intent
import com.ebook.book.service.DownloadService
import com.ebook.db.ObjectBoxManager
import com.xrn1997.common.BaseApplication

class BookApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ObjectBoxManager.init(context)
        startService(Intent(this, DownloadService::class.java))
    }
}
