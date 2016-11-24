package com.example.thusitha.autoautheticatorwfd;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class AutoAuth implements IXposedHookLoadPackage {

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> wifiP2pService = Class.forName("android.net.wifi.p2p.WifiP2pService", false, lpparam.classLoader);
            for (Class<?> c : wifiP2pService.getDeclaredClasses()) {
                //XposedBridge.log("inner class " + c.getSimpleName());
                if ("P2pStateMachine".equals(c.getSimpleName())) {
                    XposedBridge.log("AutoAuthWFD: Class " + c.getName() + " found");
                    Method notifyInvitationReceived = c.getDeclaredMethod("notifyInvitationReceived");
                    final Method sendMessage = c.getMethod("sendMessage", int.class);

                    XposedBridge.hookMethod(notifyInvitationReceived, new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                            final int PEER_CONNECTION_USER_ACCEPT = 0x00023000 + 2;
                            sendMessage.invoke(param.thisObject, PEER_CONNECTION_USER_ACCEPT);
                            return null;
                        }
                    });

                    break;
                }
            }
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
    }

}
