package com.ebook.common.view.profilePhoto

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.ebook.common.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.xrn1997.common.util.FileUtil

class PhotoCutDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private var mOnClickListener: OnPhotoClickListener? = null
    private lateinit var mPhotoUri: Uri

    // 注册用于接收 activity 结果的启动器
    private lateinit var selectLauncher: ActivityResultLauncher<PickVisualMediaRequest>
    private lateinit var takePhotoLauncher: ActivityResultLauncher<Uri>
    private lateinit var cropPhotoLauncher: ActivityResultLauncher<Intent>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //todo,未来这个Uri应该作为参数传入
        mPhotoUri = FileUtil.getPrivateFile(requireContext(), "profile.jpeg")
        selectLauncher =
            registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
                if (uri != null) {
                    gotoClipActivity(uri)
                }
            }

        takePhotoLauncher =
            registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
                if (success) {
                    gotoClipActivity(mPhotoUri)
                }
            }

        cropPhotoLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.data
                    mOnClickListener?.onScreenPhotoClick(uri)
                    dismiss()
                }
            }
    }

    fun setOnClickListener(onPhotoClickListener: OnPhotoClickListener?) {
        mOnClickListener = onPhotoClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_photo_select, container, false)
        val btnSelectPhoto = view.findViewById<Button>(R.id.btn_select_photo)
        val btnTakePhoto = view.findViewById<Button>(R.id.btn_take_photo)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)

        btnSelectPhoto.setOnClickListener(this)
        btnTakePhoto.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.btn_take_photo -> {
                takePhotoLauncher.launch(mPhotoUri)
            }

            R.id.btn_select_photo -> {
                selectLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            R.id.btn_cancel -> {
                dismiss()
            }
        }
    }

    /**
     * 打开截图界面
     */
    private fun gotoClipActivity(uri: Uri?) {
        if (uri == null) return
        val intent = Intent()
        intent.setClass(requireActivity(), ClipImageActivity::class.java)
        // 1: 圆形 2: 正方形
        val cutType = 1
        intent.putExtra("type", cutType)
        intent.setData(uri)
        cropPhotoLauncher.launch(intent)
    }

    interface OnPhotoClickListener {
        fun onScreenPhotoClick(uri: Uri?)
    }

    companion object {
        val TAG: String = PhotoCutDialog::class.java.simpleName

        //请求截图
        private const val REQUEST_CROP_PHOTO = 102

        @JvmStatic
        fun newInstance(): PhotoCutDialog {
            return PhotoCutDialog()
        }
    }
}
