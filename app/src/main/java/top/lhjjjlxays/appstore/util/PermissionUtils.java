package top.lhjjjlxays.appstore.util;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.reflect.Method;

/**
 * Created by ouyangshen on 2018/1/28.
 */
public class PermissionUtils {
    private final static String TAG = "PermissionUtils";

    // 检查某个权限。返回true表示已启用该权限，返回false表示未启用该权限
    public static void checkPermission(Activity act, String permission, int requestCode) {
        Log.d(TAG, "checkPermission: " + permission);
        // 只对Android6.0及以上系统进行校验
        // 检查当前App是否开启了名称为permission的权限
        int check = ContextCompat.checkSelfPermission(act, permission);
        if (check != PackageManager.PERMISSION_GRANTED) {
            // 未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
            ActivityCompat.requestPermissions(act, new String[]{permission}, requestCode);
        }
    }

    public static boolean checkPermission(Activity act, String permission) {
        Log.d(TAG, "checkPermission: " + permission);
        boolean result = true;
        // 只对Android6.0及以上系统进行校验
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 检查当前App是否开启了名称为permission的权限
            int check = ContextCompat.checkSelfPermission(act, permission);
            if (check != PackageManager.PERMISSION_GRANTED) {
                result = false;
            }
        }
        return result;
    }

    // 检查多个权限。返回true表示已完全启用权限，返回false表示未完全启用权限
    public static boolean checkMultiPermission(Activity act, String[] permissions, int requestCode) {
        boolean result = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int check = PackageManager.PERMISSION_GRANTED;
            // 通过权限数组检查是否都开启了这些权限
            for (String permission : permissions) {
                check = ContextCompat.checkSelfPermission(act, permission);
                if (check != PackageManager.PERMISSION_GRANTED) {
                    break;
                }
            }
            if (check != PackageManager.PERMISSION_GRANTED) {
                // 未开启该权限，则请求系统弹窗，好让用户选择是否立即开启权限
                ActivityCompat.requestPermissions(act, permissions, requestCode);
                result = false;
            }
        }
        return result;
    }

    public static void goActivity(Context ctx, Class<?> cls) {
        Intent intent = new Intent(ctx, cls);
        ctx.startActivity(intent);
    }

    private static final String[] permissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    // 获取无线网络的开关状态
    public static boolean getWlanStatus(Context ctx) {
        // 从系统服务中获取无线网络管理器
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        return wm.isWifiEnabled();
    }

    // 打开或关闭无线网络
    public static void setWlanStatus(Context ctx, boolean enabled) {
        // 从系统服务中获取无线网络管理器
        WifiManager wm = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
        assert wm != null;
        wm.setWifiEnabled(enabled);
    }

    // 打开或关闭数据连接
    public static void setMobileDataStatus(Context ctx, boolean enabled) {
        // 从系统服务中获取连接管理器
        ConnectivityManager cm = (ConnectivityManager)
                ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        try {
            String methodName = "setMobileDataEnabled"; // 这是隐藏方法，需要通过反射调用
            assert cm != null;
            Method method = cm.getClass().getMethod(methodName, Boolean.TYPE);
            // method.setAccessible(true);
            method.invoke(cm, enabled);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}