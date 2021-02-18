package com.michael.bindit.impl

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.michael.bindit.IBinding
import androidx.lifecycle.Observer
import com.michael.bindit.BindingMode


abstract class DisposableImpl : IBinding {
    protected abstract fun cleanup()

    private var alive:Boolean = true

    override fun dispose() {
        if(alive) {
            alive = false
            cleanup()
        }
    }

    override fun isDisposed(): Boolean {
        return !alive
    }
}

abstract class BaseBinding<T>(
    owner:LifecycleOwner,
    val data: LiveData<T>,
    val mode: BindingMode
) : DisposableImpl() {
    private var dataObserver:Observer<T?>? = null
    init {
        if(mode!=BindingMode.OneWayToSource) {
            dataObserver = Observer<T?> {
                onDataChanged(it)
            }.also{
                data.observe(owner,it)
            }
        }
    }

    abstract fun onDataChanged(v:T?)

    override fun cleanup() {
        val ob = dataObserver ?: return
        data.removeObserver(ob)
    }
}