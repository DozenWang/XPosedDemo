package cn.dozen.xposed;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by wangyida on 2016/6/16.
 */

public class KitkatHook {

    public static void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable {
        XposedBridge.log("initZygote : " + startupParam.modulePath);
        Class<?> activityManagerService = XposedHelpers.findClass("com.android.server.am.ActivityManagerService", null);
        XposedBridge.log("activity manager service exist : " + (activityManagerService != null));

        XposedHelpers.findAndHookMethod(activityManagerService, "startProcessLocked", "com.android.server.am.ProcessRecord", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                printCallStack();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("startProcessLocked#" + buildProcessRecord(param.args[0]) + "#" + param.args[1] + "#" + param.args[2]);
            }
        });
        XposedHelpers.findAndHookMethod(activityManagerService, "getContentProviderImpl", "android.app.IApplicationThread", "java.lang.String", "android.os.IBinder", "boolean", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);
                XposedBridge.log("getContentProviderImpl#" + buildProcessRecord(processRecord) + "#" + param.args[1]);
            }
        });

        XposedHelpers.findAndHookMethod(activityManagerService, "startService", "android.app.IApplicationThread", "android.content.Intent", "java.lang.String", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object component = param.getResult();

                Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);

                XposedBridge.log("startService#" + buildProcessRecord(processRecord) + "#" + buildIntent(param.args[1]) + "#" + component);

            }
        });

        XposedHelpers.findAndHookMethod(activityManagerService, "bindService", "android.app.IApplicationThread", "android.os.IBinder", "android.content.Intent", "java.lang.String", "android.app.IServiceConnection", "int", "int", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                Object component = param.getResult();

                Object processRecord = XposedHelpers.callMethod(param.thisObject, "getRecordForAppLocked", param.args[0]);

                XposedBridge.log("bindService#" + buildProcessRecord(processRecord) + "#" + buildIntent(param.args[2]) + "#" + component);
            }
        });


        XposedHelpers.findAndHookMethod(activityManagerService, "startServiceInPackage", "int", "android.content.Intent", "java.lang.String", "int", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("startServiceInPackage#" + param.args[0] + "#" + buildIntent(param.args[1]) + "#" + param.args[2]);
            }
        });


        Class<?> broadcustQueue = XposedHelpers.findClass("com.android.server.am.BroadcastQueue", null);
        XposedHelpers.findAndHookMethod(broadcustQueue, "processCurBroadcastLocked", "com.android.server.am.BroadcastRecord", "com.android.server.am.ProcessRecord", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("processCurBroadcastLocked#" + buildBroadcastRecord(param.args[0]) + "#" + buildProcessRecord(param.args[1]));
            }
        });


        Class<?> activeService = XposedHelpers.findClass("com.android.server.am.ActiveServices", null);
        //just for 4.3
        XposedHelpers.findAndHookMethod(activeService, "retrieveServiceLocked", "android.content.Intent", "java.lang.String", "int", "int", "int", "boolean", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param != null) {
                    Object ret = param.getResult();
                    if (ret == null) {
                        return;
                    }
                    Field recordField = XposedHelpers.findField(ret.getClass(), "record");
                    Object serviceRecord = recordField.get(ret);

                    Field componentField = XposedHelpers.findField(serviceRecord.getClass(), "name");
                    Object component = componentField.get(serviceRecord);
                    XposedBridge.log("retrieveServiceLocked#" + buildIntent(param.args[0]) + "#" + component + "#" + param.args[2] + "#" + param.args[3] + "#" + param.args[5]);

                }
            }
        });
    }

    public static void printCallStack() {
        try {
            throw new Exception("blah");
        }
        catch(Exception e) {
            for(StackTraceElement stackTraceElement : e.getStackTrace()) {
                XposedBridge.log("HookDetection : " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
            }
        }
    }


    private static String buildBroadcastRecord(Object broadcastRecord) {
        Object intent = null;
        Object callerApp = null;
        Object targetComp = null;
        try {
            if(broadcastRecord != null) {
                Field intentField = XposedHelpers.findField(broadcastRecord.getClass(), "intent");
                intent = intentField.get(broadcastRecord);

                Field targetCompField = XposedHelpers.findField(broadcastRecord.getClass(), "targetComp");
                targetComp = targetCompField.get(broadcastRecord);

                callerApp = XposedHelpers.findField(broadcastRecord.getClass(), "callerApp").get(broadcastRecord);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        StringBuffer buffer = new StringBuffer();
        buffer.append(buildIntent(intent) + "@");
        buffer.append(targetComp + "@");
        buffer.append(buildProcessRecord(callerApp) + "@");
        return buffer.toString();
    }

    private static String buildProcessRecord(Object processRecord) {
        Object pid = null, uid = null, processName = null, packageName = null;
        try {
            if(processRecord != null) {
                Field pidField = XposedHelpers.findField(processRecord.getClass(), "pid");
                pid = pidField.get(processRecord);

                Field uidField = XposedHelpers.findField(processRecord.getClass(), "uid");
                uid = uidField.get(processRecord);

                Field processNameField = XposedHelpers.findField(processRecord.getClass(), "processName");
                processName = processNameField.get(processRecord);

                Field appInfoField = XposedHelpers.findField(processRecord.getClass(), "info");
                Object appInfo = appInfoField.get(processRecord);

                Field packageNameField = XposedHelpers.findField(appInfo.getClass(), "packageName");
                packageName = packageNameField.get(appInfo);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(packageName + "@");
        buffer.append(processName + "@");
        buffer.append(uid + "@");
        buffer.append(pid);
        return buffer.toString();
    }

    private static String buildIntent(Object intent) {
        Object action = null;
        Object component = null;

        try {
            if(intent != null) {
                Field actionField = XposedHelpers.findField(intent.getClass(), "mAction");
                action = actionField.get(intent);

                Field componentField = XposedHelpers.findField(intent.getClass(), "mComponent");
                component = componentField.get(intent);
            }

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(action + "@");
        buffer.append(component);
        return buffer.toString();
    }
}
