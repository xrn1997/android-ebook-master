package com.ebook.login

import android.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.ebook.common.event.KeyCode
import com.ebook.login.databinding.ActivityVerifyUserBinding
import com.ebook.login.mvvm.factory.LoginViewModelFactory
import com.ebook.login.mvvm.viewmodel.ModifyPwdViewModel
import com.therouter.router.Route
import com.xrn1997.common.mvvm.view.BaseMvvmActivity
import kotlin.random.Random

@Route(path = KeyCode.Login.MODIFY_PATH)
class VerifyUserActivity :
    BaseMvvmActivity<ActivityVerifyUserBinding, ModifyPwdViewModel>() {
    override fun onBindViewModel(): Class<ModifyPwdViewModel> {
        return ModifyPwdViewModel::class.java
    }

    override fun onBindViewModelFactory(): ViewModelProvider.Factory {
        return LoginViewModelFactory
    }

    /**
     * todo 用真的验证码，而不是编的。
     */
    private fun getVerifyCode() {
        // 生成六位随机数字的验证码
        mViewModel.mVerifyCode = Random.nextInt(100000, 1000000).toString()
        // 弹出提醒对话框，提示用户六位验证码数字
        val builder =
            AlertDialog.Builder(getContext())
        builder.setTitle("请记住验证码")
        builder.setMessage("手机号${mViewModel.username.get()}，本次验证码是${mViewModel.mVerifyCode}，请输入验证码")
        builder.setPositiveButton("好的", null)
        val alert = builder.create()
        alert.show()
    }

    override fun initView() {
        binding.btnVerifycode.setOnClickListener { getVerifyCode() }
    }

    override fun initViewObservable() {
    }

    override fun onBindVariableId(): Int {
        return BR.viewModel
    }

    override fun onBindLayout(): Int {
        return R.layout.activity_verify_user
    }

    override fun initData() {
    }
}
