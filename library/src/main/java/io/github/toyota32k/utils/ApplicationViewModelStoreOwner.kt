package io.github.toyota32k.utils

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

object ApplicationViewModelStoreOwner : ViewModelStoreOwner {
    val mViewModelStore:ViewModelStore by lazy { ViewModelStore() }

    override fun getViewModelStore(): ViewModelStore {
        return mViewModelStore
    }

    // to be called from Application.onTerminate()
    fun releaseViewModelStore() {
        mViewModelStore.clear()
    }
}