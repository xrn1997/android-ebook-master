package com.ebook.common.util;

import android.Manifest;
import android.content.Intent;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.ToastUtils;
import com.ebook.common.BuildConfig;
import com.permissionx.guolindev.PermissionMediator;
import com.permissionx.guolindev.PermissionX;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import me.nereo.multi_image_selector.MultiImageSelector;

/**
 * Description: <h3>多媒体工具类</h3>
 * <ul>
 * <li>1.图片选择器，可算多张图片</li>
 * <li>2.拍照</li>
 * <li>3.拍视频</li>
 * <li>4.创建一个图片路径</li>
 * <li>5.创建一个视频路径</li>
 * </ul>
 * <h3>注意事项：</h3>
 * <ul><li>1. 拍照、拍视频、选择图片完成的回调都在onActivityResult中回调的</l1>
 * <li>2.选择图片获取：List<String> path = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT)</li>
 * </ul>
 */
public class MultiMediaUtil {
    private static final String TAG = "MultiMediaUtil";
    public static final int SELECT_IMAGE = 1001;
    public static final int TAKE_PHONE = 1002;
    public static final int TAKE_VIDEO = 1003;

    /**
     * 打开图片选择器，选择图片<br>
     * 来获取图片
     *
     * @param count：选择图片个数
     */
    public static void photoSelect(FragmentActivity activity, int count, int requestCode) {
        photoSelect(activity, null, count, requestCode);
    }

    public static void photoSelect(Fragment fragment, int count, int requestCode) {
        photoSelect(null, fragment, count, requestCode);
    }

    private static void photoSelect(final FragmentActivity activity, final Fragment fragment, final int count, final int requestCode) {
        if (activity == null && fragment == null) {
            return;
        }
        PermissionMediator permissionX;
        if (activity != null) {
            permissionX = PermissionX.init(activity);
        } else {
            permissionX = PermissionX.init(fragment);
        }
        permissionX.permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "即将重新申请的权限是程序必须依赖的权限(请选择始终)", "我已明白", "取消"))
                .onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        if (activity != null) {
                            MultiImageSelector.create().showCamera(false).count(count).single().multi()
                                    //.origin(ArrayList<String>)
                                    .start(activity, requestCode);
                        } else {
                            MultiImageSelector.create().showCamera(false).count(count).single().multi()
                                    //.origin(ArrayList<String>)
                                    .start(fragment, requestCode);
                        }
                    } else {
                        ToastUtils.showShort("无读写外部存储设备权限");
                    }
                });
    }

    /**
     * 拍照
     *
     * @param path:照片存放的路径
     * @param requestCode  {@link MultiMediaUtil#TAKE_PHONE}
     */
    public static void takePhoto(FragmentActivity activity, String path, int requestCode) {
        takePhoto(activity, null, path, requestCode);
    }

    public static void takePhoto(Fragment fragment, String path, int requestCode) {
        takePhoto(null, fragment, path, requestCode);
    }

    private static void takePhoto(final FragmentActivity activity, final Fragment fragment, final String path, final int requestCode) {
        if (activity == null && fragment == null) {
            return;
        }
        PermissionMediator permissionX;
        if (activity != null) {
            permissionX = PermissionX.init(activity);
        } else {
            permissionX = PermissionX.init(fragment);
        }
        permissionX.permissions(Manifest.permission.CAMERA)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "即将重新申请的权限是程序必须依赖的权限(请选择始终)", "我已明白", "取消"))
                .onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        File file = new File(path);

                        try {
                            if (file.createNewFile()) {
                                Intent intent = new Intent();
                                intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                if (activity != null) {
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, BuildConfig.LIBRARY_PACKAGE_NAME, file));
                                    activity.startActivityForResult(intent, requestCode);
                                } else {
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(fragment.getContext(), BuildConfig.LIBRARY_PACKAGE_NAME, file));
                                    fragment.startActivityForResult(intent, requestCode);
                                }
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "takePhoto: ", e);
                            ToastUtils.showShort("无法启动拍照程序");
                        }
                    } else {
                        ToastUtils.showShort("无摄像头权限,无法进行拍照!");
                    }
                });
    }


    /**
     * 拍视频
     *
     * @param path:视频存放的路径
     */
    public static void takeVideo(final FragmentActivity activity, final String path, final int requestCode) {
        takeVideo(activity, null, path, requestCode);
    }

    public static void takeVideo(final Fragment fragment, final String path, final int requestCode) {
        takeVideo(null, fragment, path, requestCode);
    }

    private static void takeVideo(final FragmentActivity activity, final Fragment fragment, final String path, final int requestCode) {
        if (activity == null && fragment == null) {
            return;
        }
        PermissionMediator permissionX;
        if (activity != null) {
            permissionX = PermissionX.init(activity);
        } else {
            permissionX = PermissionX.init(fragment);
        }
        permissionX.permissions(Manifest.permission.CAMERA)
                .onExplainRequestReason((scope, deniedList) -> scope.showRequestReasonDialog(deniedList, "即将重新申请的权限是程序必须依赖的权限(请选择始终)", "我已明白", "取消"))
                .onForwardToSettings((scope, deniedList) -> scope.showForwardToSettingsDialog(deniedList, "您需要去应用程序设置当中手动开启权限", "我已明白", "取消"))
                .request((allGranted, grantedList, deniedList) -> {
                    if (allGranted) {
                        File file = new File(path);
                        try {
                            if (file.createNewFile()) {
                                Intent intent = new Intent();
                                intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);
                                intent.addCategory(Intent.CATEGORY_DEFAULT);
                                //intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                                if (activity != null) {
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(activity, BuildConfig.LIBRARY_PACKAGE_NAME, file));
                                    activity.startActivityForResult(intent, requestCode);
                                } else {
                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(fragment.getContext(), BuildConfig.LIBRARY_PACKAGE_NAME, file));
                                    fragment.startActivityForResult(intent, requestCode);
                                }

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "takeVideo: ", e);
                            ToastUtils.showShort("无法启动拍视频程序");
                        }
                    } else {
                        ToastUtils.showShort("无摄像头权限,无法进行拍视频!");
                    }
                });
    }

    //获取图片路径
    public static String getPhotoPath(AppCompatActivity activity) {
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) + ".jpg";
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir != null) {
            return dir.getAbsolutePath() + File.separator + filename;
        } else {
            return null;
        }
    }

    //获取视频的路径
    public static String getVideoPath(AppCompatActivity activity) {
        String filename = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date()) + ".3gp";
        File dir = activity.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
        if (dir != null) {
            return dir.getAbsolutePath() + File.separator + filename;
        } else {
            return null;
        }
    }

}
