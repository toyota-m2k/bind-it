@file:Suppress("unused")

package io.github.toyota32k.bindit.command

import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.*

/**
 * LifecycleOwnerが死んでいる間にinvoke()されても、次に bind()した時点で、ちゃんとコールバックすることができるCommandクラス。
 * サブスレッドからコマンドを invoke()するとき、Activityが生きている保証がない場合に使用する。
 * ただし、bind()できるlifecycleOwnerとリスナーは１つに限定される。
 * bindForever()も実装してはいるが、Lifecycleを無視するなら、LiteCommandで十分のはず。
 */
class ReliableCommand<T:Any>() : CommandBase<T>() {
    constructor(callback:(T)->Unit):this() {
        bindForever(callback)
    }
    private val subject = SingleLiveData<T>()
    private var disposable:IDisposable? = null

    override fun bind(owner: LifecycleOwner, fn: (T) -> Unit): IDisposable {
        disposable?.dispose()
        return subject.disposableObserve(owner, fn).apply { disposable = this }
    }

    override fun bindForever(fn: (T) -> Unit): IDisposable {
        utTenderAssert(false) { "use LiteCommand instead." }
        disposable?.dispose()
        return subject.disposableObserveForever(fn).apply { disposable = this }
    }

    override fun reset() {
        dispose()
    }

    override fun dispose() {
        disposable?.dispose()
        disposable = null
    }

    override fun invoke(value: T) {
        subject.value = value
    }
}

/**
 * コールバックに引数を取らないコマンドクラス
 */
class ReliableUnitCommand private constructor(rc: ReliableCommand<Unit>): UnitCommand(rc) {
    constructor():this(ReliableCommand<Unit>())
    constructor(callback:(Unit)->Unit):this(ReliableCommand(callback))
}
