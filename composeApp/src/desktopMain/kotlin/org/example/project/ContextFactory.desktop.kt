package org.example.project

actual class ContextFactory {

    actual fun getContext(): Any = 0
    actual fun getApplication(): Any = 0
    actual fun getActivity(): Any = 0
}