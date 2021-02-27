@file:Suppress("unused")

package com.michael.bindit.util

import java.util.concurrent.atomic.DoubleAccumulator


fun <T> List<T>.reverse():Iterable<T> {
    return Iterable {
        iterator {
            for (i in count()-1 downTo 0) {
                yield(get(i)!!)
            }
        }
    }
}

fun <S,E> Collection<E>.reduce(accumulator:S, fn:(acc:S,element:E)->S): S {
    var acc = accumulator
    forEach {
        fn(acc,it)
    }
    return acc
}



//fun utAssert(f:Boolean, msg:(()->String?)?=null) {
//    if (BuildConfig.DEBUG && !f) {
//        error(msg?.invoke() ?: "Assertion failed")
//    }
//}