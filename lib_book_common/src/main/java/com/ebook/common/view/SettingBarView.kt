package com.ebook.common.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import com.ebook.common.R
import com.xrn1997.common.adapter.binding.BindingCommand

class SettingBarView(context: Context, attrs: AttributeSet?) :
    RelativeLayout(context, attrs) {
    private val imgLeftIcon: ImageView?
    private val txtSetContent: EditText?
    private val layoutSettingBar: RelativeLayout
    private var mOnClickSettingBarViewListener: OnClickSettingBarViewListener? = null
    private var mOnClickRightImgListener: OnClickRightImgListener? = null
    private var isEdit = false //是否需要编辑
    private var mOnViewChangeListener: OnViewChangeListener? = null

    init {
        inflate(context, R.layout.view_setting_bar, this)
        layoutSettingBar = findViewById(R.id.layout_setting_bar)
        imgLeftIcon = findViewById(R.id.img_start_icon)
        val txtSetTitle = findViewById<TextView>(R.id.txt_set_title)
        txtSetContent = findViewById(R.id.txt_set_content)
        val imgRightIcon = findViewById<ImageView>(R.id.img_end_icon)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.SettingBarView)
        val isVisibleLeftIcon =
            typedArray.getBoolean(R.styleable.SettingBarView_set_left_icon_visible, false)
        val isVisibleRightIcon =
            typedArray.getBoolean(R.styleable.SettingBarView_set_right_icon_visible, false)
        val title = typedArray.getString(R.styleable.SettingBarView_set_title)
        val hint = typedArray.getString(R.styleable.SettingBarView_set_content_hint)
        val content = typedArray.getString(R.styleable.SettingBarView_set_content)
        val rightIcon = typedArray.getResourceId(R.styleable.SettingBarView_set_right_icon, 0)
        val leftIcon = typedArray.getResourceId(R.styleable.SettingBarView_set_left_icon, 0)
        typedArray.recycle()

        imgLeftIcon.visibility = if (isVisibleLeftIcon) VISIBLE else GONE
        txtSetTitle.text = title
        txtSetContent.hint = hint
        txtSetContent.setText(content)
        imgRightIcon.visibility = if (isVisibleRightIcon) VISIBLE else GONE
        if (leftIcon > 0) {
            imgLeftIcon.setImageResource(leftIcon)
        }
        if (rightIcon > 0) {
            imgRightIcon.setImageResource(rightIcon)
        }
        imgRightIcon.setOnClickListener {
            if (mOnClickRightImgListener != null) {
                mOnClickRightImgListener!!.onClick()
            }
        }
        layoutSettingBar.setOnClickListener {
            if (mOnClickSettingBarViewListener != null) {
                mOnClickSettingBarViewListener!!.onClick()
            }
        }
        txtSetContent.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                Log.v(TAG, "onTextChanged start....")
                if (mOnViewChangeListener != null) {
                    mOnViewChangeListener!!.onChange()
                }
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    fun setOnClickRightImgListener(onClickRightImgListener: OnClickRightImgListener) {
        mOnClickRightImgListener = onClickRightImgListener
    }

    fun setOnClickSettingBarViewListener(onClickSettingBarViewListener: OnClickSettingBarViewListener) {
        mOnClickSettingBarViewListener = onClickSettingBarViewListener
    }

    var content: String?
        get() {
            if (txtSetContent != null) {
                return txtSetContent.text.toString()
            }
            return null
        }
        set(value) {
            if (txtSetContent != null && !TextUtils.isEmpty(value)) {
                txtSetContent.setText(value)
            }
        }

    private fun setViewChangeListener(listener: OnViewChangeListener?) {
        this.mOnViewChangeListener = listener
    }

    fun setEnableEditContext(b: Boolean) {
        isEdit = b
        if (txtSetContent != null) {
            txtSetContent.isEnabled = b
        }
    }

    fun showImgLeftIcon(show: Boolean) {
        if (imgLeftIcon != null) {
            imgLeftIcon.visibility = if (show) VISIBLE else GONE
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return !isEdit
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return layoutSettingBar.onTouchEvent(event)
    }

    interface OnClickSettingBarViewListener {
        fun onClick()
    }

    interface OnClickRightImgListener {
        fun onClick()
    }

    private interface OnViewChangeListener {
        fun onChange()
    }

    companion object {
        const val TAG: String = "SettingBarView"

        @BindingAdapter(value = ["content"], requireAll = false)
        fun setContent(view: SettingBarView, value: String) {
            if (!TextUtils.isEmpty(view.content) && view.content == value) {
                return
            }
            if (view.txtSetContent != null && !TextUtils.isEmpty(value)) {
                view.txtSetContent.setText(value)
            }
        }

        @InverseBindingAdapter(attribute = "content", event = "contentAttrChanged")
        fun getContent(view: SettingBarView): String? {
            return view.content
        }

        @BindingAdapter(value = ["contentAttrChanged"], requireAll = false)
        fun setDisplayAttrChanged(
            view: SettingBarView,
            inverseBindingListener: InverseBindingListener?
        ) {
            Log.d(TAG, "setDisplayAttrChanged")

            if (inverseBindingListener == null) {
                view.setViewChangeListener(null)
                Log.d(TAG, "setViewChangeListener(null)")
            } else {
                view.setViewChangeListener(object : OnViewChangeListener {
                    override fun onChange() {
                        Log.d(TAG, "setDisplayAttrChanged -> onChange")
                        inverseBindingListener.onChange()
                    }
                })
            }
        }

        @BindingAdapter(value = ["onClickSettingBarView"], requireAll = false)
        fun onClickSettingBarView(view: SettingBarView, bindingCommand: BindingCommand?) {
            view.layoutSettingBar.setOnClickListener {
                bindingCommand?.execute()
            }
        }
    }
}
