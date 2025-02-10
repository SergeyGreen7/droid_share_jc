package org.example.project

// "composeApp/src/commonMain/.../ContextFactory.kt" file
expect class ContextFactory {
    fun getContext(): Any
    fun getApplication(): Any
    fun getActivity(): Any
}