package com.mengnieyu.helloworld;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class PermissionUtil {
    private final String TAG = "PermissionUtil";
    private Activity mContext;

    //自己的项目包名
    private String packageName = mContext.getPackageName();
    private static PermissionUtil Instance;
    private  int PERMISSION_SETTING_FOR_RESULT=1;

    public static PermissionUtil getInstance(Activity context) {
        if (Instance == null) {
            Instance = new PermissionUtil(context);
        }
        return Instance;
    }



    public PermissionUtil(Activity context) {
        this.mContext = context;
        this.packageName = mContext.getPackageName();
    }


    public void GoToSetting() {
        goIntentSetting();
    }

    private void goIntentSetting() {
        try {
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            if (mContext != null) {
                mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            String name = Build.MANUFACTURER;
            Log.d(TAG, "jumpPermissionPage --- name : " + name);
            switch (name) {
                case "HUAWEI":
                    goHuaWeiMainager();
                    break;
                case "vivo":
                    goVivoMainager();
                    break;
                case "OPPO":
                    goOppoMainager();
                    break;
                case "Coolpad":
                    goCoolpadMainager();
                    break;
                case "Meizu":
                    goMeizuMainager();
                    break;
                case "Xiaomi":
                    goXiaoMiMainager();
                    break;
                case "Sony":
                    goSonyMainager();
                    break;
                case "LG":
                    goLGMainager();
                    break;
                default:
                    systemConfig();
                    break;
            }
        }

    }

    private void goLGMainager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
            intent.setComponent(comp);
            if (mContext != null) {
                mContext.startActivityForResult(intent,PERMISSION_SETTING_FOR_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            systemConfig();
        }

    }

    private void goSonyMainager() {
        try {
            Intent intent = new Intent(packageName);
            ComponentName comp = new ComponentName("com.sonymobile.cta", "com.sonymobile.cta.SomcCTAMainActivity");
            intent.setComponent(comp);
            if (mContext != null) {
                mContext.startActivityForResult(intent,PERMISSION_SETTING_FOR_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            systemConfig();
        }

    }

    private void goHuaWeiMainager() {
        try {
            Intent intent = new Intent(packageName);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ComponentName comp = new ComponentName("com.huawei.systemmanager", "com.huawei.permissionmanager.ui.MainActivity");
            intent.setComponent(comp);
            if (mContext != null) {
                mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
            }
        } catch (Exception e) {
            e.printStackTrace();
            systemConfig();
        }

    }

    private static String getMiuiVersion() {
        String propName = "ro.miui.ui.version.name";
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;

    }

    private void goXiaoMiMainager() {
        String rom = getMiuiVersion();

        Intent intent = new Intent();
        if ("V6".equals(rom) || "V7".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.AppPermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
            if (mContext != null) {
                mContext.startActivityForResult(intent,PERMISSION_SETTING_FOR_RESULT);
            }
        } else if ("V8".equals(rom) || "V9".equals(rom)) {
            intent.setAction("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", packageName);
            if (mContext != null) {
                mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
            }
        } else {
            systemConfig();
        }
    }

    private void goMeizuMainager() {
        try {
            Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC");
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra("packageName", packageName);
            if (mContext != null) {
                mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
            }
        } catch (ActivityNotFoundException localActivityNotFoundException) {
            localActivityNotFoundException.printStackTrace();
            systemConfig();
        }

    }



    /**
     * 系统设置界面
     */
    private void systemConfig() {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        if (mContext != null) {
            mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
        }
    }

    private void goOppoMainager() {
        doStartApplicationWithPackageName("com.coloros.safecenter");
    }

    /**
     * doStartApplicationWithPackageName("com.yulong.android.security:remote")
     * <p>
     * 和Intent open = getPackageManager().getLaunchIntentForPackage("com.yulong.android.security:remote");
     * <p>
     * startActivity(open);
     * <p>
     * 本质上没有什么区别，通过Intent open...打开比调用doStartApplicationWithPackageName方法更快，也是android本身提供的方法
     */
    private void goCoolpadMainager() {
        doStartApplicationWithPackageName("com.yulong.android.security:remote");
    }

    private void goVivoMainager() {
        doStartApplicationWithPackageName("com.bairenkeji.icaller");
    }

    /**
     * 此方法在手机各个机型设置中已经失效
     *
     * @return
     */
    private Intent getAppDetailSettingIntent() {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        localIntent.setData(Uri.fromParts("package", mContext.getPackageName(), null));
        return localIntent;
    }

    private void doStartApplicationWithPackageName(String packagename) {
        // 通过包名获取此APP详细信息，包括Activities、services、versioncode、name等等
        PackageInfo packageinfo = null;
        try {
            packageinfo = mContext.getPackageManager().getPackageInfo(packagename, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageinfo == null) {
            return;
        }
        // 创建一个类别为CATEGORY_LAUNCHER的该包名的Intent
        Intent resolveIntent = new Intent(Intent.ACTION_MAIN, null);
        resolveIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveIntent.setPackage(packageinfo.packageName);
        // 通过getPackageManager()的queryIntentActivities方法遍历
        List<ResolveInfo> resolveinfoList = mContext.getPackageManager()
                .queryIntentActivities(resolveIntent, 0);


        ResolveInfo resolveinfo = resolveinfoList.iterator().next();
        if (resolveinfo != null) {
            // packageName参数2 = 参数 packname
            String packageName = resolveinfo.activityInfo.packageName;
            // 这个就是我们要找的该APP的LAUNCHER的Activity[组织形式：packageName参数2.mainActivityname]
            String className = resolveinfo.activityInfo.name;
            // LAUNCHER Intent
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            // 设置ComponentName参数1:packageName参数2:MainActivity路径
            ComponentName cn = new ComponentName(packageName, className);
            intent.setComponent(cn);
            try {
                if (mContext != null) {
                    mContext.startActivityForResult(intent, PERMISSION_SETTING_FOR_RESULT);
                }
            } catch (Exception e) {
                systemConfig();
                e.printStackTrace();
            }
        }
    }
}