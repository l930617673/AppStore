/*
 * Copyright 2016 jeasonlzy(廖子尧)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package top.lhjjjlxays.appstore.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import top.lhjjjlxays.appstore.bean.ApkInfo;

/**
 * ================================================
 * 作    者：jeasonlzy（廖子尧）Github地址：https://github.com/jeasonlzy
 * 版    本：1.0
 * 创建日期：2016/1/25
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class ApkUtils {

    private static final String TAG = ApkUtils.class.getSimpleName();

    /**
     * 安装一个apk文件
     */
    public static void install(Context context, File uriFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri uri = FileProvider.getUriForFile(context, "com.example.network.provider", uriFile);
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(intent);
    }

    /**
     * 卸载一个app
     */
    public static void uninstall(Context context, String packageName) {
        //通过程序的包名创建URI
        Uri packageURI = Uri.parse("package:" + packageName);
        //创建Intent意图
        Intent intent = new Intent(Intent.ACTION_DELETE, packageURI);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //执行卸载程序
        context.startActivity(intent);
    }

    // 获取指定应用已经安装的版本号
    public static String getInstallVersion(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 获取设备上面所有已经存在着的APK文件
    public static ArrayList<ApkInfo> getAllApkFile(Context context) {
        ArrayList<ApkInfo> appAray = new ArrayList<ApkInfo>();
        // 查找本地所有的apk文件，其中mime_type指定了APK的文件类型
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Files.getContentUri("external"),
                null, "mime_type=\"application/vnd.android.package-archive\"", null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                // 获取文件名
                String title = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.TITLE));
                // 获取文件完整路径
                String path = cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                // 获取文件大小
                long size = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                PackageManager pm = context.getPackageManager();
                // 获取apk文件的包信息
                PackageInfo pi = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
                if (pi != null) {
                    String pkg_name = pi.packageName; // 包名
                    String vs_name = pi.versionName; // 版本名称
                    int vs_code = pi.versionCode; // 版本号
                    // 将该记录添加到apk文件信息列表
                    appAray.add(new ApkInfo(title, path, size, pkg_name, vs_name, vs_code));
                }
            }
            cursor.close(); // 关闭数据库游标
        }

        File path = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        PackageManager pm = context.getPackageManager();
        assert path != null;
        for (File file : Objects.requireNonNull(path.listFiles())) {
            if (file.getName().endsWith(".apk")) {
                PackageInfo pi = pm.getPackageArchiveInfo(file.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                if (pi != null) {
                    String pkg_name = pi.packageName; // 包名
                    String vs_name = pi.versionName; // 版本名称
                    int vs_code = pi.versionCode; // 版本号
                    // 将该记录添加到apk文件信息列表
                    appAray.add(new ApkInfo(file.getName(), file.getAbsolutePath(), file.length(), pkg_name, vs_name, vs_code));
                }
            }
        }

        return appAray;
    }

    // 获取指定文件的安装包信息
    public static ApkInfo getApkInfo(Context context, String path) {
        ApkInfo info = new ApkInfo();
        PackageManager pm = context.getPackageManager();
        // 从指定路径的APK文件中解析应用的包信息，包括包名、版本号等等
        PackageInfo pi = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (pi != null) {
            Log.d(TAG, "packageName=" + pi.packageName + ", versionName=" + pi.versionName);
            info.file_path = path;
            info.package_name = pi.packageName; // 包名
            info.version_name = pi.versionName; // 版本名称
            info.version_code = pi.versionCode; // 版本号
        }
        return info;
    }
}
