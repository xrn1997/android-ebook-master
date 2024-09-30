package com.ebook.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.blankj.utilcode.util.ToastUtils;

@SuppressWarnings("unused")
public class NetUtil {
    private TelephonyManager telephonyManager;
    private PhoneStateListener phoneStateListener;

    public static boolean checkNet(Context context) {
        return isWifiConnection(context) || isMobileConnection(context);
    }

    public static boolean checkNetToast(Context context) {
        boolean isNet = checkNet(context);
        if (!isNet) {
            ToastUtils.showShort("网络不给力哦！");
        }
        return isNet;
    }

    /**
     * 是否使用移动网络
     */
    public static boolean isMobileConnection(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        if (manager != null) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
        }
        return false;
    }

    /**
     * 是否使用WIFI联网
     */
    public static boolean isWifiConnection(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        if (manager != null) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            return capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        return false;
    }

    public static NetType getNetworkType(Context context) {
        ConnectivityManager manager = context.getSystemService(ConnectivityManager.class);
        if (manager != null) {
            NetworkCapabilities capabilities = manager.getNetworkCapabilities(manager.getActiveNetwork());
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return NetType.WIFI;
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return NetType.MOBILE_NET; // 移动网络
                }
            }
        }
        return NetType.NO_NET;
    }

    public enum NetType {WIFI, MOBILE_NET, NO_NET}
}
