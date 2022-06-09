package io.github.toyota32k.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.Closeable

/**
 * Flow による監視可能なフラグクラス。
 */
class UtObservableFlag (private val flag:MutableStateFlow<Boolean> = MutableStateFlow(false)) : Flow<Boolean> by flag {
//    fun set() = synchronized(this) {
//        flag.value = true
//    }
//    fun reset() = synchronized(this) {
//        flag.value = false
//    }
    val value get() = synchronized(this) { flag.value }

    fun set():Boolean = synchronized(this) {
        if(!flag.value) {
            flag.value = true
            true
        } else false
    }
    fun reset():Boolean = synchronized(this) {
        if(flag.value) {
            flag.value = false
            true
        } else false
    }

    inline fun <T> ifSet(def:T, fn:()->T):T {
        return if(set()) {
            try {
                fn()
            } finally {
                reset()
            }
        } else def
    }

    inline fun ifSet(fn:()->Unit):Boolean {
        return if(set()) {
            try {
                fn()
            } finally {
                reset()
            }
            true
        } else false
    }

    private inner class Closer : Closeable {
        override fun close() {
            reset()
        }
    }

    fun closeableSetFlag():Closeable? {
        return if(set()) {
            Closer()
        } else null
    }
}