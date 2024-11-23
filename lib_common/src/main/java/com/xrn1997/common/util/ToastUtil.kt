package com.xrn1997.common.util

import android.content.Context
import android.widget.Toast

object ToastUtil {
    @JvmStatic
    fun showShort(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
    }

    @JvmStatic
    fun showLong(context: Context, text: CharSequence) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }
}

