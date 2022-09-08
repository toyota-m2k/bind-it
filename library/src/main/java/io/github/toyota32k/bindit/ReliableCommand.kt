package io.github.toyota32k.bindit

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.annotation.MainThread
import androidx.lifecycle.*
import io.github.toyota32k.utils.*

/**
 * invoke()が呼ばれたときに、直接コールバックしないで、LiveData (SingleObserverEvent) -> observer を介してイベントを発行するコマンドクラス。
 * Command や、LiteCommand は、invoke()されると、LifecycleOwnerの状態に関わらず（除 Destroyed）、すぐにコールバックするのに対して、
 * ReliableCommandは、LifecycleOwnerがRESUMEやSTOPされた状態の場合は、すぐにはコールバックしないで、次にSTART状態になったタイミングでコールされる。
 * つまり、サブスレッドからActivity上のViewやFragmentを操作するようなケースにこれを使う。
 * ただ、リスナーが１つだけなら、SingleObserverEvent を使えばよいと思う。
 */
class ReliableCommand<T>(initialValue:T) : ICommand<T> {
    constructor(initialValue: T, callback:(T)->Unit):this(initialValue) {
        bindForever(callback)
    }

    var value:T = initialValue
    private inner class SingleObserverEvent(owner: LifecycleOwner, val callback:(T)->Unit): IDisposable, LifecycleEventObserver, Observer<Boolean> {
        private val trigger = SingleLiveEvent<Boolean>()

        init {
            trigger.observe(owner, this)
        }

        override fun dispose() {
            trigger.removeObserver(this)
            events.remove(this)
        }

        override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
            if(!source.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
                dispose()
            }
        }

        fun invoke() {
            trigger.fire(true)
        }

        override fun onChanged(flag: Boolean?) {
            if(flag==true) {
                callback(value)
            }
        }
    }
    private inner class DisposableCallback(private val callback:(T)->Unit):IDisposable {
        fun invoke() {
            callback.invoke(value)
        }
        override fun dispose() {
            foreverCallbacks.remove(this)
        }

    }
    private val events = mutableListOf<SingleObserverEvent>()
    private val foreverCallbacks = mutableListOf<DisposableCallback>()

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
        return Command.ClickListenerDisposer(view)
    }

    @MainThread
    override fun bind(owner: LifecycleOwner, fn: (T) -> Unit): IDisposable {
        return SingleObserverEvent(owner,fn).apply {
            events.add(this)
        }
    }

    @MainThread
    override fun bindForever(fn: (T) -> Unit): IDisposable {
        return DisposableCallback(fn).apply {
            foreverCallbacks.add(this)
        }
    }

    @MainThread
    override fun attachAndBind(owner: LifecycleOwner, view: View, value:T, fn: (T) -> Unit): IDisposable {
        internalAttachView(view, value)
        return Command.ClickListenerDisposer(view, bind(owner, fn))
    }

    @MainThread
    override fun reset() {
        while(events.size>0) {
            val orgSize = events.size
            events.last().dispose()
            if(orgSize == events.size) {
                UtLog.libLogger.assert(false, "cannot dispose event listeners.")
                break
            }
        }
        events.clear()
        foreverCallbacks.clear()
    }

    @MainThread
    override fun dispose() {
        reset()
    }

    @MainThread
    override fun invoke(value:T) {
        this.value = value
        foreverCallbacks.forEach { it.invoke() }
        events.forEach { it.invoke() }
    }
}

@Suppress("unused")
class ReliableUnitCommand() : UnitCommand(ReliableCommand(Unit)) {
    constructor(forever:()->Unit):this() {
        bindForever(forever)
    }
}