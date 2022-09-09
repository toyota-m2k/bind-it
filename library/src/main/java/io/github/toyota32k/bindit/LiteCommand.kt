package io.github.toyota32k.bindit

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.*

/**
 * invoke()から直接コールバックするライトウェイトコマンドクラス。
 * ButtonのonClickハンドラとして使うだけならこれで十分。
 * 従来の Command クラスは、引数に役に立たない view を持っていたりして使いづらかったのだが、こちらは、任意の型を渡せるようにした。
 * たとえば、Okボタンなら、invoke(true)、キャンセルボタンなら invoke(false) のように使い分けることを想定。
 * 一方、ボタンクリック以外に、サブスレッド（タスク）からinvoke され、ActivityやViewを操作するようなハンドラを呼び出す必要があるときは、
 * ReliableCommand を使うべき。
 */
class LiteCommand<T>() : ICommand<T> {
    constructor(callback:(T)->Unit):this() {
        bindForever(callback)
    }

    private val listeners = Listeners<T>()

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

    override fun attachView(view: View, value: T): IDisposable {
        internalAttachView(view, value)
        return Command.ClickListenerDisposer(view)
    }

    override fun bind(owner: LifecycleOwner, fn: (T) -> Unit): IDisposable {
        return listeners.add(owner,fn)
    }

    override fun bindForever(fn: (T) -> Unit): IDisposable {
        return listeners.addForever(fn)
    }

    override fun attachAndBind(owner: LifecycleOwner, view: View, value: T, fn: (T) -> Unit): IDisposable {
        attachView(view, value)
        return Command.ClickListenerDisposer(view, bind(owner, fn))
    }

    override fun reset() {
        listeners.clear()
    }

    override fun dispose() {
        reset()
    }

    override fun invoke(value: T) {
        listeners.invoke(value)
    }
}

class LiteUnitCommand() : UnitCommand(LiteCommand<Unit>()) {
    constructor(callback:()->Unit):this() {
        bindForever(callback)
    }
}