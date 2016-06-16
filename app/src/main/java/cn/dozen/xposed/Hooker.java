package cn.dozen.xposed;

import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by wangyida on 2016/6/16.
 */

public class Hooker {

    public static void hookStartProcessLocked(Class<?> am) throws Throwable {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            XposedHelpers.findAndHookMethod(am, "startProcessLocked", "com.android.server.am.ProcessRecord", String.class, String.class, String.class, String.class, String[].class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Util.printCallStack();
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("startProcessLocked#" + Util.buildProcessRecord(param.args[0]) + "#" + param.args[1] + "#" + param.args[2]);
                }
            });
        } else {
            XposedHelpers.findAndHookMethod(am, "startProcessLocked", "com.android.server.am.ProcessRecord", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Util.printCallStack();
                }

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    XposedBridge.log("startProcessLocked#" + Util.buildProcessRecord(param.args[0]) + "#" + param.args[1] + "#" + param.args[2]);
                }
            });
        }
    }

    public static void hookGetContentProviderImpl(Class<?> am) {
        XposedHelpers.findAndHookMethod(am, "getContentProviderImpl", "android.app.IApplicationThread", "java.lang.String", "android.os.IBinder", "boolean", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                XposedBridge.log("getContentProviderImpl#" + Util.buildProcessRecord(processRecord) + "#" + param.args[1]);
            }
        });
    }

    public static void hookStartService(Class<?> am) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod(am, "startService", "android.app.IApplicationThread", "android.content.Intent", "java.lang.String", "java.lang.String", "int", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object component = param.getResult();
                    Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                    XposedBridge.log("startService#" + Util.buildProcessRecord(processRecord) + "#" + Util.buildIntent(param.args[1]) + "#" + component);

                }
            });
        } else {
            XposedHelpers.findAndHookMethod(am, "startService", "android.app.IApplicationThread", "android.content.Intent", "java.lang.String", "int", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object component = param.getResult();
                    Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                    XposedBridge.log("startService#" +  Util.buildProcessRecord(processRecord) + "#" +  Util.buildIntent(param.args[1]) + "#" + component);

                }
            });
        }
    }

    public static void hookBindService(Class<?> am) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            XposedHelpers.findAndHookMethod(am, "bindService", "android.app.IApplicationThread", "android.os.IBinder", "android.content.Intent", "java.lang.String", "android.app.IServiceConnection", "int", String.class, "int", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object component = param.getResult();
                    Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                    XposedBridge.log("bindService#" + Util.buildProcessRecord(processRecord) + "#" + Util.buildIntent(param.args[2]) + "#" + component);
                }
            });
        } else {
            XposedHelpers.findAndHookMethod(am, "bindService", "android.app.IApplicationThread", "android.os.IBinder", "android.content.Intent", "java.lang.String", "android.app.IServiceConnection", "int", "int", new XC_MethodHook() {

                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object component = param.getResult();
                    Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                    XposedBridge.log("bindService#" + Util.buildProcessRecord(processRecord) + "#" + Util.buildIntent(param.args[2]) + "#" + component);
                }
            });
        }
    }
    public static void hookProcessBroadcastLocked(Class<?> broadcastqueue) {
        XposedHelpers.findAndHookMethod(broadcastqueue, "processCurBroadcastLocked", "com.android.server.am.BroadcastRecord", "com.android.server.am.ProcessRecord", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                XposedBridge.log("processCurBroadcastLocked#" + Util.buildBroadcastRecord(param.args[0]) + "#" + Util.buildProcessRecord(param.args[1]));
            }
        });
    }
}
