package debug;

import com.ebook.api.RetrofitManager;
import com.ebook.common.BaseApplication;


public class FindApplication extends BaseApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        RetrofitManager.init(this);
        GreenDaoManager.init(this);
    }
}
