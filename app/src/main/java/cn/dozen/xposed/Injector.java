package cn.dozen.xposed;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;


/**
 * Created by wangyida on 2016/6/13.
 */

public class Injector implements IXposedHookLoadPackage {
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if(lpparam.packageName.equals("android")) {
            Class<?> am = Class.forName("com.android.server.am.ActivityManagerService", false, lpparam.classLoader);
            Class<?> broadcastQueue = XposedHelpers.findClass("com.android.server.am.BroadcastQueue", lpparam.classLoader);
            Class<?> activeServices = XposedHelpers.findClass("com.android.server.am.ActiveServices", lpparam.classLoader);

            Hooker.hookStartProcessLocked(am);
            Hooker.hookGetContentProviderImpl(am);
            Hooker.hookStartService(am);
            Hooker.hookBindService(am);
            Hooker.hookProcessBroadcastLocked(broadcastQueue);
            Hooker.hookRetrieveServiceLocked(activeServices);
            Hooker.hookProcessNextBroadcast(broadcastQueue);
        }
    }
}
