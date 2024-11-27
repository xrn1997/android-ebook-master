package com.ebook.login

import androidx.lifecycle.ViewModelProvider
import com.ebook.common.event.KeyCode
import com.ebook.login.databinding.ActivityRegisterBinding
import com.ebook.login.mvvm.factory.LoginViewModelFactory
import com.ebook.login.mvvm.viewmodel.RegisterViewModel
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmActivity

@Route(path = KeyCode.Login.REGISTER_PATH)
class RegisterActivity :
    BaseMvvmActivity<ActivityRegisterBinding, RegisterViewModel>() {
    override fun onBindLayout(): Int {
        return R.layout.activity_register
    }

    override fun onBindViewModel(): Class<RegisterViewModel> {
        return RegisterViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return LoginViewModelFactory
    }

    override fun initViewObservable() {
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun initView() {
    }

    override fun initData() {
    }
}