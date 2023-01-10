package io.github.toyota32k.utils

open class Disposer : IDisposableEx {
    final override var disposed: Boolean = false
        private set

    protected val disposables = mutableListOf<IDisposable>()
    override fun dispose() {
        if(!disposed) {
            disposed = true
            reset()
        }
    }

    fun register(vararg disposables:IDisposable):Disposer {
        for(b in disposables) {
            this.disposables.add(b)
        }
        return this
    }

    fun unregister(vararg disposables:IDisposable?):Disposer {
        for(b in disposables) {
            if(b!=null && this.disposables.contains(b)) {
                this.disposables.remove(b)
                b.dispose()
            }
        }
        return this
    }

    var clientData:Any? = null

    fun reset() {
        disposables.forEach { it.dispose() }
        disposables.clear()

        // clientData が disposableならdispose()してnullにする。
        // disposableでなければ何もしない。
        val cd = clientData as? IDisposable ?: return
        cd.dispose()
        clientData = null
    }

    fun clean() {
        val itr = disposables.iterator()
        while(itr.hasNext()) {
            val v = itr.next() as? IDisposableEx ?: continue
            if(v.disposed) {
                itr.remove()
            }
        }
    }
}
