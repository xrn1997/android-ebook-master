package com.ebook.login

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.ebook.login.databinding.ActivityModifyPwdBinding
import com.ebook.login.mvvm.factory.LoginViewModelFactory
import com.ebook.login.mvvm.viewmodel.ModifyPwdViewModel
import com.hwangjr.rxbus.RxBus
import com.xrn1997.common.mvvm.view.BaseMvvmActivity

class ModifyPwdActivity :
    BaseMvvmActivity<ActivityModifyPwdBinding, ModifyPwdViewModel>() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RxBus.get().register(this)
    }

    public override fun onDestroy() {
        super.onDestroy()
        RxBus.get().unregister(this)
    }

    override fun onBindViewModel(): Class<ModifyPwdViewModel> {
        return ModifyPwdViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return LoginViewModelFactory
    }

    override fun initViewObservable() {
    }

    override fun initData() {
        val bundle = this.intent.extras
        if (bundle != null) {
            val username = bundle.getString("username")
            mViewModel.username.set(username)
        }
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun onBindLayout(): Int {
        return R.layout.activity_modify_pwd
    }

    override fun initView() {
    }
}
