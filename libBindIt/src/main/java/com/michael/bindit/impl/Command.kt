package com.michael.bindit.impl

import android.view.View
import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import com.michael.bindit.util.IDisposable
import com.michael.bindit.util.Listeners
import java.lang.ref.WeakReference

class Command : View.OnClickListener {
    private val listeners = Listeners<View?>()
    private class ClickListenerDisposer(v:View, var bind: IDisposable?=null) : IDisposable {
        var view:WeakReference<View>? = WeakReference<View>(v)

        override fun dispose() {
            bind?.dispose()
            view?.get()?.apply {
                setOnClickListener(null)
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
        view.setOnClickListener(this)
    }

    @MainThread
    fun connectViewEx(view:View) : IDisposable {
        view.setOnClickListener(this)
        return ClickListenerDisposer(view)
    }


    override fun onClick(v: View?) {
        listeners.invoke(v)
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
}