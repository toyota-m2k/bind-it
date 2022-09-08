package io.github.toyota32k.utils

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.CoroutineContext

/**
 * Flow を監視可能な Observer クラス
 * 監視解除は、dispose()で。
 */
class DisposableFlowObserver<T> constructor(flow: Flow<T>, coroutineContext: CoroutineContext, private val callback:(v:T)->Unit): IDisposableEx {
    constructor(flow: Flow<T>, callback:(v:T)->Unit) : this(flow, Dispatchers.Main, callback)
    constructor(flow: Flow<T>, owner: LifecycleOwner, callback:(v:T)->Unit) : this(flow, owner.lifecycleScope.coroutineContext, callback)
    private var scope: CoroutineScope? =
        CoroutineScope(coroutineContext+ SupervisorJob()).apply {
            flow.onEach {
                callback(it)
            }.launchIn(this)
        }

    override fun dispose() {
        scope?.cancel()
        scope = null
    }

    override val disposed: Boolean
        get() = scope == null
}

/**
 * Flow にオブザーバーを登録し、登録解除用の IDisposable を返す。
 */
fun <T> Flow<T>.disposableObserve(owner: LifecycleOwner, callback:(v:T)->Unit):DisposableFlowObserver<T> =
    DisposableFlowObserver(this, owner, callback)
/**
 * Flow にオブザーバーを登録し、登録解除用の Closeable を返す。
 */
fun <T> Flow<T>.disposableObserve(coroutineContext: CoroutineContext, callback:(v:T)->Unit):DisposableFlowObserver<T> =
    DisposableFlowObserver(this, coroutineContext, callback)
