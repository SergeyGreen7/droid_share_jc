package org.example.project

import android.annotation.SuppressLint
import androidx.core.app.ComponentActivity

// "composeApp/src/commonMain/.../ContextFactory.kt" file
actual class ContextFactory(
    @SuppressLint("RestrictedApi") private val activity: ComponentActivity
) {
    actual fun getContext(): Any = activity.baseContext
    actual fun getApplication(): Any = activity.application
    actual fun getActivity(): Any = activity
}