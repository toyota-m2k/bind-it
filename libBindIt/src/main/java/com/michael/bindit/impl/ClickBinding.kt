@file:Suppress("unused")

package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import com.michael.bindit.BindingMode
import com.michael.bindit.IBinding
import com.michael.bindit.util.Callback
import com.michael.bindit.util.ListenerKey
import com.michael.bindit.util.Listeners

class ClickBinding<V> (
        owner:LifecycleOwner,
        val view: V,
        val listeners :Listeners<View>,
        fn:((View)->Unit),
) : IBinding, View.OnClickListener where V:View {
    constructor(owner:LifecycleOwner, view:V, fn:((View)->Unit)) : this(owner,view,Listeners<View>(), fn)
    override val mode: BindingMode = BindingMode.OneWayToSource
    var key: ListenerKey? = null
    init {
        view.setOnClickListener(this)
        key = listeners.add(owner,fn)
    }

    override fun onClick(v: View?) {
        listeners.invoke(view)
    }

    override fun dispose() {
        key?.dispose()
        key = null
    }

    override fun isDisposed(): Boolean {
        return key!=null
    }
}

class LongClickBinding<V>(
    owner: LifecycleOwner,
    val view: V,
    fn: (V)->Boolean
) : IBinding, View.OnLongClickListener where V:View {
    override val mode: BindingMode = BindingMode.OneWayToSource
    @Suppress("MemberVisibilityCanBePrivate")
    var callback : Callback<V,Boolean>? = Callback(owner,fn)
    init {
        view.setOnLongClickListener(this)
    }

    override fun onLongClick(v: View?): Boolean {
        return callback?.invoke(view) ?: false
    }
    override fun dispose() {
        callback?.dispose()
        callback = null
    }

    override fun isDisposed(): Boolean {
        return callback!=null
    }
}