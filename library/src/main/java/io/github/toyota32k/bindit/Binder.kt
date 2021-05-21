@file:Suppress("unused")

package io.github.toyota32k.bindit

import io.github.toyota32k.utils.IDisposable

@Suppress("MemberVisibilityCanBePrivate")
open class Binder : IDisposable {
    protected var disposed: Boolean = false
    protected val bindings = mutableListOf<IDisposable>()
    override fun dispose() {
        if(!disposed) {
            disposed = true
            reset()
        }
    }

    override fun isDisposed(): Boolean {
        return disposed
    }

    fun register(vararg bindings:IDisposable):Binder {
        for(b in bindings) {
            this.bindings.add(b)
        }
        return this
    }

    fun reset() {
        bindings.forEach { it.dispose() }
        bindings.clear()
    }
}
