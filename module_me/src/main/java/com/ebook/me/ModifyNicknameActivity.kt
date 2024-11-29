package com.ebook.me

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.ebook.me.databinding.ActivityModifyNicknameBinding
import com.ebook.me.mvvm.factory.MeViewModelFactory
import com.ebook.me.mvvm.viewmodel.ModifyViewModel
import com.xrn1997.common.mvvm.view.BaseMvvmActivity

class ModifyNicknameActivity : BaseMvvmActivity<ActivityModifyNicknameBinding, ModifyViewModel>() {

    override fun initView() {
        binding.idBtnRegister.setOnClickListener {
            mViewModel.modifyNickname(binding.idEtReg1stPwd.text.toString())
        }
    }

    override fun initData() {
    }

    override fun onBindViewModel(): Class<ModifyViewModel> {
        return ModifyViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return MeViewModelFactory
    }

    override fun onBindViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        attachToParent: Boolean
    ): ActivityModifyNicknameBinding {
        return ActivityModifyNicknameBinding.inflate(inflater, parent, attachToParent)
    }
}
