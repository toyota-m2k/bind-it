package io.github.toyota32k.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.io.Closeable

/**
 * LiveDataのobserverをIDisposable#dispose()やCloseable#close() によって登録解除できるようにするクラス
 */
class DisposableObserver<T>(data: LiveData<T>, owner: LifecycleOwner, private val callback:(v:T?)->Unit): Observer<T?>, IDisposable, Closeable {
    private var data: LiveData<T>? = data
    init {
        data.observe(owner, this)
    }
    override fun onChanged(t: T?) {
        callback(t)
    }
    override fun dispose() {
        data?.removeObserver(this)
        data = null
    }

    override fun close() =dispose()
}

/**
 * LiveDataにオブザーバーを登録し、登録解除用の IDisposable または、Closeable を返す。
 */
fun <T> LiveData<T>.disposableObserve(owner: LifecycleOwner, fn:(v:T?)->Unit) : IDisposable
        = DisposableObserver(this,owner,fn)

fun <T> LiveData<T>.closableObserve(owner: LifecycleOwner, fn:(v:T?)->Unit) : Closeable
        = DisposableObserver(this,owner,fn)
