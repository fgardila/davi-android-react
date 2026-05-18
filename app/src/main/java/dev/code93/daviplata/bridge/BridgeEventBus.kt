package dev.code93.daviplata.bridge

import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import java.lang.ref.WeakReference
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BridgeEventBus @Inject constructor() {

    private var reactContextRef: WeakReference<ReactContext?> = WeakReference(null)
    private val pending = mutableListOf<Pair<String, WritableMap?>>()

    fun attach(context: ReactContext) {
        reactContextRef = WeakReference(context)
        val pendingCopy = pending.toList()
        pending.clear()
        pendingCopy.forEach { (name, params) -> emit(name, params) }
    }

    fun emit(eventName: String, params: WritableMap? = null) {
        val ctx = reactContextRef.get()
        if (ctx != null && ctx.hasActiveReactInstance()) {
            ctx.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit(eventName, params)
        } else {
            pending.add(eventName to params)
        }
    }
}
