package io.github.toyota32k.utils

import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner

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

interface IUnitCommand : IDisposable {
    @MainThread
    fun attachView(view: View) : IDisposable

    @MainThread
    fun bind(owner: LifecycleOwner, fn: () -> Unit): IDisposable

    @MainThread
    fun bindForever(fn:()->Unit): IDisposable

    @MainThread
    fun attachAndBind(owner: LifecycleOwner, view: View, fn:(()->Unit)):IDisposable

    @MainThread
    fun reset()

    @MainThread
    fun invoke()
}

abstract class UnitCommand(private val cmd:ICommand<Unit>):IUnitCommand {
    override fun attachView(view:View):IDisposable
            = cmd.attachView(view, Unit)
    override fun attachAndBind(owner: LifecycleOwner, view: View, fn: () -> Unit): IDisposable
            = cmd.attachAndBind(owner, view, Unit) { fn() }
    override fun bind(owner:LifecycleOwner, fn:()->Unit):IDisposable
            = cmd.bind(owner) { _:Unit->fn() }
    override fun bindForever(fn:()->Unit):IDisposable
            = cmd.bindForever { _:Unit->fn() }
    override fun invoke()
            = cmd.invoke(Unit)
    override fun reset()
            = cmd.reset()
    override fun dispose()
            = cmd.dispose()
}