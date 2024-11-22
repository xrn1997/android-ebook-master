package com.ebook.common.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.ebook.common.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DeleteDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private var mOnClickListener: OnDeleteClickListener? = null

    fun setOnClickListener(onDeleteClickListener: OnDeleteClickListener?) {
        mOnClickListener = onDeleteClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_delete_dialog, container, false)
        val btnDelete = view.findViewById<Button>(R.id.btn_delete)
        val btnCancel = view.findViewById<Button>(R.id.btn_cancel)
        btnDelete.setOnClickListener(this)
        btnCancel.setOnClickListener(this)
        return view
    }

    override fun onClick(v: View) {
        val i = v.id
        if (i == R.id.btn_delete) {
            if (mOnClickListener != null) {
                mOnClickListener!!.onItemClick()
            }
            dismiss()
        } else if (i == R.id.btn_cancel) {
            dismiss()
        }
    }

    interface OnDeleteClickListener {
        fun onItemClick()
    }

    companion object {
        val TAG: String = DeleteDialog::class.java.simpleName

        @JvmStatic
        fun newInstance(): DeleteDialog {
            return DeleteDialog()
        }
    }
}
