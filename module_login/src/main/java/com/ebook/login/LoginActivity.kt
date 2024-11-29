package com.ebook.login

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ebook.common.event.KeyCode
import com.ebook.login.databinding.ActivityLoginBinding
import com.ebook.login.mvvm.factory.LoginViewModelFactory
import com.ebook.login.mvvm.viewmodel.LoginViewModel
import com.hwangjr.rxbus.RxBus
import com.therouter.router.Autowired
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmActivity

@Route(path = KeyCode.Login.LOGIN_PATH)
class LoginActivity : BaseMvvmActivity<ActivityLoginBinding, LoginViewModel>() {
    @Autowired
    @JvmField
    var path: String = String()
    private var mBundle: Bundle? = null //储存被拦截的信息

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.get().register(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
    }


    override fun onBindViewModel(): Class<LoginViewModel> {
        return LoginViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return LoginViewModelFactory
    }

    /**
     * 禁止显示Toolbar，默认为true
     */
    override fun enableToolbar(): Boolean {
        return false
    }

    override fun enableFitsSystemWindows(): Boolean {
        return false
    }

    override fun initView() {
        binding.idTvRegister.setOnClickListener { toRegisterActivity() }
        binding.idTvFgtPwd.setOnClickListener { toForgetPwdActivity() }
        binding.idBtnLogin.setOnClickListener {
            val username = binding.idEtUsername.text.toString()
            val password = binding.idEtPassword.text.toString()
            mViewModel.login(username, password)
        }
    }

    override fun initData() {}
    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): ActivityLoginBinding {
        return ActivityLoginBinding.inflate(inflater, parent, attachToParent)
    }

    override fun onStart() {
        super.onStart()
        val bundle = this.intent.extras
        var username: String? = null
        var password: String? = null
        if (bundle != null && !bundle.isEmpty) {
            username = bundle.getString("username")
            password = bundle.getString("password")
        }
        if (username.isNullOrEmpty() && password.isNullOrEmpty()) {
            binding.idEtUsername.setText(username)
            binding.idEtPassword.setText(password)
        }
        if (!TextUtils.isEmpty(path) && mBundle == null) {
            mViewModel.path = path
            if (bundle != null && !bundle.isEmpty) {
                mBundle = bundle
                mViewModel.bundle = mBundle
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun toRegisterActivity() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun toForgetPwdActivity() {
        val intent = Intent(this, VerifyUserActivity::class.java)
        startActivity(intent)
    }

}
