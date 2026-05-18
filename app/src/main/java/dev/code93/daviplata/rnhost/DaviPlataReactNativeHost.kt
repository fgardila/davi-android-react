package dev.code93.daviplata.rnhost

import android.app.Application
import android.content.pm.ApplicationInfo
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.shell.MainReactPackage

object DaviPlataReactNativeHost {

    lateinit var host: ReactNativeHost
        private set

    fun init(app: Application, extraPackages: List<ReactPackage> = emptyList()) {
        host = object : ReactNativeHost(app) {
            override fun getUseDeveloperSupport(): Boolean =
                app.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0

            override fun getPackages(): List<ReactPackage> =
                listOf(MainReactPackage()) + extraPackages

            override fun getJSMainModuleName(): String = "index"

            override fun getBundleAssetName(): String = "index.android.bundle"
        }
    }

    val isInitialized: Boolean get() = ::host.isInitialized
}
