package debug

import com.ebook.common.BaseApplication
import com.ebook.db.ObjectBoxManager

class FindApplication : BaseApplication() {
    override fun onCreate() {
        super.onCreate()
        ObjectBoxManager.init(context)
    }
}
