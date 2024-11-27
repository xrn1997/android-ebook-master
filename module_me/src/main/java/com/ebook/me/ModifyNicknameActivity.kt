package com.ebook.me

import androidx.lifecycle.ViewModelProvider
import com.ebook.me.databinding.ActivityModifyNicknameBinding
import com.ebook.me.mvvm.factory.MeViewModelFactory
import com.ebook.me.mvvm.viewmodel.ModifyViewModel
import com.xrn1997.common.mvvm.view.BaseMvvmActivity

class ModifyNicknameActivity :
    BaseMvvmActivity<ActivityModifyNicknameBinding, ModifyViewModel>() {
    override fun onBindViewModel(): Class<ModifyViewModel> {
        return ModifyViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return MeViewModelFactory
    }

    override fun initViewObservable() {
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun onBindLayout(): Int {
        return R.layout.activity_modify_nickname
    }

    override fun initView() {
    }

    override fun initData() {
    }
}
