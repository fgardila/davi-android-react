package dev.code93.daviplata.security

import android.content.Context
import com.scottyab.rootbeer.RootBeer
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RootDetector @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    fun isRooted(): Boolean = RootBeer(context).isRooted
}
