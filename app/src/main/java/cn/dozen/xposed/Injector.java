package cn.dozen.xposed;

import java.lang.reflect.Field;

import de.robv.android.xposed.IXposedHookZygoteInit;


/**
 * Created by wangyida on 2016/6/13.
 */

public class Injector implements IXposedHookZygoteInit {


    @Override
    public void initZygote(StartupParam startupParam) throws Throwable {

    }
}
