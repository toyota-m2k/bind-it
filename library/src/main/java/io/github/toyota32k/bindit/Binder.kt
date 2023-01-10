@file:Suppress("unused")

package io.github.toyota32k.bindit

import io.github.toyota32k.utils.Disposer
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.IDisposableEx
import java.util.concurrent.locks.Condition

@Suppress("MemberVisibilityCanBePrivate")
open class Binder : Disposer() {
    val bindings get() = disposables

    operator fun plus(binding:IDisposable):Binder {
        return add(binding)
    }
    operator fun minus(binding:IDisposable):Binder {
        return remove(binding)
    }

    fun add(vararg bindings:IDisposable):Binder {
        register(*bindings)
        return this
    }

    fun remove(vararg bindings:IDisposable):Binder {
        unregister(*bindings)
        return this
    }

    fun add(fn:()->IDisposable?):Binder {
        fn()?.apply {
            register(this)
        }
        return this
    }
    fun conditional(condition: Boolean, fn:Binder.()->Unit):Binder {
        if(condition) {
            fn()
        }
        return this
    }
}