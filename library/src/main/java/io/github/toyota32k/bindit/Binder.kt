@file:Suppress("unused")

package io.github.toyota32k.bindit

import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.IDisposableEx

@Suppress("MemberVisibilityCanBePrivate")
open class Binder : IDisposable {
    var disposed: Boolean = false
        private set
    protected val bindings = mutableListOf<IDisposable>()
    override fun dispose() {
        if(!disposed) {
            disposed = true
            reset()
        }
    }

    fun register(vararg bindings:IDisposable):Binder {
        for(b in bindings) {
            this.bindings.add(b)
        }
        return this
    }

    fun unregister(vararg bindings:IDisposable?):Binder {
        for(b in bindings) {
            if(b!=null && this.bindings.contains(b)) {
                this.bindings.remove(b)
                b.dispose()
            }
        }
        return this
    }

    var clientData:Any? = null

    fun reset() {
        bindings.forEach { it.dispose() }
        bindings.clear()

        // clientData が disposableならdispose()してnullにする。
        // disposableでなければ何もしない。
        val cd = clientData as? IDisposable ?: return
        cd.dispose()
        clientData = null
    }

    fun clean() {
        val itr = bindings.iterator()
        while(itr.hasNext()) {
            val v = itr.next() as? IDisposableEx ?: continue
            if(v.disposed) {
                itr.remove()
            }
        }
    }
}
