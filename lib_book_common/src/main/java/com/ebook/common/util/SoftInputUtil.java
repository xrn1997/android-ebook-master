package com.ebook.common.util;

import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Description: <软键盘的显示与隐藏><br>
 * <ul>
 * <li>1.显示软键盘</li>
 * <li>2.隐藏软键盘</li>
 * </ul>
 */
public class SoftInputUtil {

    /**
     * 显示软键盘
     */
    public static void showSoftInput(Context context) {
        InputMethodManager imm = context.getSystemService(InputMethodManager.class); // 显示软键盘
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 显示软键盘
     */
    public static void showSoftInput(Context context, View view) {
        InputMethodManager imm = context.getSystemService(InputMethodManager.class); // 显示软键盘
        imm.showSoftInput(view, 0);
    }

    /**
     * 隐藏软键盘
     */
    public static void hideSoftInput(Context context, View view) {
        InputMethodManager immHide = context.getSystemService(InputMethodManager.class); // 隐藏软键盘
        immHide.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}