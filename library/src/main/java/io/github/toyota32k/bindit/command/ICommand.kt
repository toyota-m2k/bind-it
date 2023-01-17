@file:Suppress("unused", "PackageDirectoryMismatch")

package io.github.toyota32k.utils

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.bindit.Binder
import java.lang.ref.WeakReference

/**
 * コマンドクラスのif定義
 */
interface ICommand<T>: IDisposable {
    @MainThread
    fun attachView(view: View, value: T) : IDisposable

    @MainThread
    fun bind(owner: LifecycleOwner, fn: (T) -> Unit): IDisposable

    @MainThread
    fun bindForever(fn:(T)->Unit): IDisposable

    @MainThread
    fun attachAndBind(owner: LifecycleOwner, view: View, value:T, fn:((T)->Unit)):IDisposable

    @MainThread
    fun reset()

    @MainThread
    fun invoke(value:T)
}

/**
 * attachViewに関する共通実装
 */
abstract class CommandBase<T> : ICommand<T> {
    class ClickListenerDisposer(v:View, var bind:IDisposable?=null) : IDisposable {
        var view: WeakReference<View>? = WeakReference<View>(v)

        override fun dispose() {
            bind?.dispose()
            view?.get()?.apply {
                if(this is EditText) {
                    setOnEditorActionListener(null)
                } else {
                    setOnClickListener(null)
                }
            }
            view = null
            bind = null
        }
    }

    private fun internalAttachView(view: View, value: T) {
        if(view is EditText) {
            view.setOnEditorActionListener {_,actionId,event->
                if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
                    invoke(value)
                    true
                } else false
            }
        } else {
            view.setOnClickListener {
                invoke(value)
            }
        }
    }

    @MainThread
    override fun attachView(view: View, value: T): IDisposable {
        internalAttachView(view, value)
        return ClickListenerDisposer(view)
    }

    @MainThread
    override fun attachAndBind(owner: LifecycleOwner, view: View, value:T, fn: (T) -> Unit): IDisposable {
        internalAttachView(view, value)
        return ClickListenerDisposer(view, bind(owner, fn))
    }
}

/**
 * コールバックに引数を取らないコマンドクラスのi/f
 * ICommand<Unit> そのものなのだが、invoke(Unit) などと書かないといけないのは、かっちょ悪すぎるので。
 */
interface IUnitCommand {
    @MainThread
    fun invoke()

    @MainThread
    fun attachAndBind(owner: LifecycleOwner, view: View, fn: () -> Unit): IDisposable

    @MainThread
    fun attachView(view: View):IDisposable

    @MainThread
    fun bind(owner: LifecycleOwner, fn: () -> Unit): IDisposable

    @MainThread
    fun bindForever(fn:()->Unit): IDisposable

    @MainThread
    fun reset()
}

open class UnitCommand(private val command:ICommand<Unit>) : IUnitCommand {
    override fun invoke()
        = command.invoke(Unit)
    override fun attachAndBind(owner: LifecycleOwner, view: View, fn: () -> Unit): IDisposable
        = command.attachAndBind(owner,view,Unit) { fn() }
    override fun attachView(view: View):IDisposable
        = command.attachView(view,Unit)
    override fun bind(owner: LifecycleOwner, fn: () -> Unit): IDisposable
        = command.bind(owner) { fn() }

    override fun bindForever(fn: () -> Unit): IDisposable
        = command.bindForever { fn() }

    override fun reset()
        = command.reset()
}

fun <T> Binder.bindCommand(owner: LifecycleOwner, cmd:ICommand<T>, vararg views:Pair<View,T>, callback:(T)->Unit): Binder {
    views.forEach {  pair->
        add(cmd.attachView(pair.first, pair.second))
    }
    add(cmd.bind(owner,callback))
    return this
}

fun <T> Binder.bindCommand(cmd:ICommand<T>, vararg views:Pair<View,T>, callback:(T)->Unit): Binder
    = bindCommand(requireOwner, cmd, views=views, callback)
fun <T> Binder.bindCommand(cmd:ICommand<T>, view:View, param:T, callback:(T)->Unit): Binder
        = bindCommand(cmd, Pair(view,param),callback=callback)
fun <T> Binder.bindCommand(cmd:ICommand<T>, vararg views:Pair<View,T>): Binder {
    views.forEach {  pair->
        add(cmd.attachView(pair.first, pair.second))
    }
    return this
}
fun <T> Binder.bindCommand(cmd:ICommand<T>, view:View, param:T): Binder
    = bindCommand(cmd, Pair(view,param))




fun Binder.bindCommand(cmd:IUnitCommand, vararg views:View): Binder {
    views.forEach { view->
        add(cmd.attachView(view))
    }
    return this
}
fun Binder.bindCommand(owner: LifecycleOwner, cmd:IUnitCommand, vararg views:View, callback:()->Unit): Binder
        = bindCommand(cmd,*views).add(cmd.bind(owner,callback))

fun Binder.bindCommand(cmd:IUnitCommand, vararg views:View, callback:()->Unit): Binder
        = bindCommand(requireOwner, cmd, views=views, callback)


