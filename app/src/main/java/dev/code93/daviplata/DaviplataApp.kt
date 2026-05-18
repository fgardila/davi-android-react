package dev.code93.daviplata

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.soloader.OpenSourceMergedSoMapping
import com.facebook.soloader.SoLoader
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.EntryPointAccessors
import dev.code93.daviplata.bridge.DaviPlataBridgePackage
import dev.code93.daviplata.di.BridgeEntryPoint
import dev.code93.daviplata.rnhost.DaviPlataReactNativeHost

@HiltAndroidApp
class DaviPlataApp : Application(), ReactApplication {

    override val reactNativeHost: ReactNativeHost
        get() = DaviPlataReactNativeHost.host

    override fun onCreate() {
        super.onCreate()
        // RN 0.76+ consolidates ~60 separate .so files into libreactnative.so using a merge-map
        // (pre_merge_jni_libraries section). SoLoader.init with OpenSourceMergedSoMapping
        // registers that map so that loadLibrary("react_featureflagsjni") resolves to the
        // code embedded inside libreactnative.so instead of looking for a standalone .so file.
        SoLoader.init(this, OpenSourceMergedSoMapping)

        val ep = EntryPointAccessors.fromApplication(this, BridgeEntryPoint::class.java)
        val bridgePackage = DaviPlataBridgePackage(
            ep.getBalanceUseCase(),
            ep.getMovementsUseCase(),
            ep.clearSessionUseCase(),
            ep.bridgeEventBus(),
            ep.secureStorage(),
        )
        DaviPlataReactNativeHost.init(this, listOf(bridgePackage))
    }
}
