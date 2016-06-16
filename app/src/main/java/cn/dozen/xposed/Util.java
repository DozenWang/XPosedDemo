package cn.dozen.xposed;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by wangyida on 2016/6/16.
 */

public class Util {

    public static void printCallStack() {
        try {
            throw new Exception("blah");
        } catch (Exception e) {
            for (StackTraceElement stackTraceElement : e.getStackTrace()) {
                XposedBridge.log("HookDetection : " + stackTraceElement.getClassName() + "->" + stackTraceElement.getMethodName());
            }
        }
    }

    public static String buildBroadcastRecord(Object broadcastRecord) {
        Object intent = null;
        Object callerApp = null;
        Object targetComp = null;
        try {
            if (broadcastRecord != null) {
                Field intentField = XposedHelpers.findField(broadcastRecord.getClass(), "intent");
                intent = intentField.get(broadcastRecord);

                //compat 4.2 no targetComp field
                try {
                    Field targetCompField = XposedHelpers.findField(broadcastRecord.getClass(), "targetComp");
                    targetComp = targetCompField.get(broadcastRecord);
                } catch (NoSuchFieldError e) {
                    targetComp = null;
                }

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

    public static String buildProcessRecord(Object processRecord) {
        Object pid = null, uid = null, processName = null, packageName = null;
        try {
            if (processRecord != null) {
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

    public static String buildIntent(Object intent) {
        Object action = null;
        Object component = null;

        try {
            if (intent != null) {
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
