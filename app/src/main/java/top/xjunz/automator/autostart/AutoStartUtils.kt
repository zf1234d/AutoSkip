package top.xjunz.automator.autostart

import android.content.ComponentName
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.system.Os
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.ShizukuProvider.MANAGER_APPLICATION_ID
import rikka.shizuku.SystemServiceHelper
import rikka.sui.Sui
import top.xjunz.automator.BuildConfig
import top.xjunz.automator.app.AutomatorApp

/**
 * @author xjunz 2021/8/16
 */
private val shizukuAutoStartComponentName by lazy {
    ComponentName(MANAGER_APPLICATION_ID, "moe.shizuku.manager.starter.BootCompleteReceiver")
}
private val myAutoStartComponentName by lazy {
    ComponentName(BuildConfig.APPLICATION_ID, AutoStarter::class.java.name)
}
private val packageManager by lazy {
    AutomatorApp.me.packageManager
}

fun enableShizukuAutoStart(): Boolean {
    runCatching {
        if (!isShizukuAutoStartEnabled()) {
            if (Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                val ipm = IPackageManager.Stub.asInterface(ShizukuBinderWrapper(SystemServiceHelper.getSystemService("package")))
                ipm.setComponentEnabledSetting(
                    shizukuAutoStartComponentName,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP, Os.getuid() / 100000
                )
            } else {
                return false
            }
        }
    }.onSuccess {
        return true
    }.onFailure {
        it.printStackTrace()
        return false
    }
    return false
}

fun isAutoStartEnabled(): Boolean {
    return if(Sui.isSui()){
        isComponentEnabled(myAutoStartComponentName)
    }
    else{
        isShizukuAutoStartEnabled()
    }
}

fun setAutoStartComponentEnable(enabled: Boolean) {
    val oldState = packageManager.getComponentEnabledSetting(myAutoStartComponentName)
    val newState = if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED
    if (newState != oldState) {
        packageManager.setComponentEnabledSetting(myAutoStartComponentName, newState, PackageManager.DONT_KILL_APP)
    }
}

fun isShizukuAutoStartEnabled(): Boolean {
    return isComponentEnabled(shizukuAutoStartComponentName)
}

private fun isComponentEnabled(componentName: ComponentName, def: Boolean = true): Boolean {
    return try {
        when (packageManager.getComponentEnabledSetting(componentName)) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> def
            else -> false
        }
    } catch (t: Throwable) {
        false
    }
}