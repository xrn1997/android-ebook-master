package debug;

import android.content.Intent;

import com.ebook.book.service.DownloadService;
import com.xrn1997.common.BaseApplication;


public class BookApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        startService(new Intent(this, DownloadService.class));
    }
}
