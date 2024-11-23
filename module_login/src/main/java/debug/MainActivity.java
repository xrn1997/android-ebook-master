package debug;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SPUtils;
import com.ebook.common.event.KeyCode;
import com.ebook.login.R;
import com.therouter.TheRouter;
import com.xrn1997.common.util.ToastUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListener();
    }

    private void initListener() {
        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_register).setOnClickListener(this);
        findViewById(R.id.btn_interrupt).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            TheRouter.build(KeyCode.Login.LOGIN_PATH)
                    .navigation();
        } else if (id == R.id.btn_register) { // 注册
            TheRouter.build(KeyCode.Login.REGISTER_PATH)
                    .withString("msg", "TheRouter传递过来的不需要登录的参数msg")
                    .navigation();
        } else if (id == R.id.btn_interrupt) { // 拦截测试
            TheRouter.build(KeyCode.Login.TEST_INTERRUPT_PATH)
                    .withString("msg", "TheRouter传递过来的需要登录的参数msg")
                    .navigation();
        } else if (id == R.id.btn_exit) { // 退出登录
            ToastUtil.showShort(this, "退出登录成功");
            SPUtils.getInstance().remove(KeyCode.Login.SP_IS_LOGIN);
        }
    }
}
