package dev.code93.daviplata.presentation.home

import android.os.Bundle
import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultReactActivityDelegate
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeReactActivity : ReactActivity() {

    override fun getMainComponentName(): String = "DaviPlataRN"

    override fun createReactActivityDelegate(): ReactActivityDelegate =
        object : DefaultReactActivityDelegate(this, mainComponentName, false) {
            override fun getLaunchOptions(): Bundle = Bundle().apply {
                putString("screen", intent.getStringExtra("screen") ?: "HOME")
                putString("userId", intent.getStringExtra("userId") ?: "")
                putString("name", intent.getStringExtra("name") ?: "")
            }
        }
}
