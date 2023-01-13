package io.github.toyota32k.utils

import androidx.annotation.MainThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

/**
 * LifecycleOwnerを、その生存期間だけ安全に保持するクラス
 */
class LifecycleOwnerHolder(owner: LifecycleOwner?=null, private val onDestroyed:()->Unit) : LifecycleEventObserver {
    var lifecycleOwner:LifecycleOwner? = null
        @MainThread
        set(owner) {
            field?.lifecycle?.apply {
                removeObserver(this@LifecycleOwnerHolder)
            }
            field = owner
            owner?.lifecycle?.addObserver(this)
        }

    init {
        lifecycleOwner = owner
    }

    /**
     * LifecycleEventObserver.onStateChanged
     */
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if(!source.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            onDestroyed()
            close()
        }
    }

    @MainThread
    private fun close() {
        lifecycleOwner = null
    }
}