package io.github.toyota32k.bindit

import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.Listeners
import java.lang.ref.WeakReference

class Command : View.OnClickListener, TextView.OnEditorActionListener {
    private val listeners = Listeners<View?>()
    private class ClickListenerDisposer(v:View, var bind:IDisposable?=null) : IDisposable {
        var view:WeakReference<View>? = WeakReference<View>(v)

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

        override fun isDisposed(): Boolean {
            return view!=null
        }

    }

    @MainThread
    fun connectView(view:View) {
        if(view is EditText) {
            view.setOnEditorActionListener(this)
        } else {
            view.setOnClickListener(this)
        }
    }

    @MainThread
    fun connectViewEx(view:View) : IDisposable {
        connectView(view)
        return ClickListenerDisposer(view)
    }

    @MainThread
    fun bind(owner: LifecycleOwner, fn:((View?)->Unit)): IDisposable {
        return listeners.add(owner,fn)
    }

    @MainThread
    fun connectAndBind(owner: LifecycleOwner, view:View, fn:((View?)->Unit)):IDisposable {
        connectView(view)
        return ClickListenerDisposer(view, bind(owner,fn))
    }

    @MainThread
    fun reset() {
        listeners.clear()
    }

    override fun onClick(v: View?) {
        listeners.invoke(v)
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return if (actionId == EditorInfo.IME_ACTION_DONE || event?.action == KeyEvent.ACTION_DOWN && (event.keyCode == KeyEvent.KEYCODE_ENTER || event.keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER)) {
            listeners.invoke(v)
            true
        } else false
    }
}