package debug

import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ebook.book.R
import com.ebook.book.databinding.ActivityMainBinding
import com.ebook.book.fragment.MainBookFragment.Companion.newInstance
import com.xrn1997.common.mvvm.view.BaseActivity
import com.xrn1997.common.util.ToastUtil.showShort
import kotlin.system.exitProcess

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private var exitTime: Long = 0


    override fun initView() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frame_content, newInstance()).commit()
    }

    override fun enableToolbar(): Boolean {
        return false
    }

    override fun initData() {
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    private fun exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            showShort(this, "再按一次退出程序")
            exitTime = System.currentTimeMillis()
        } else {
            finish()
            exitProcess(0)
        }
    }


    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): ActivityMainBinding {
        return ActivityMainBinding.inflate(inflater, parent, attachToParent)
    }
}
