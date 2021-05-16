@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import io.github.toyota32k.bindit.BindingMode
import io.github.toyota32k.bindit.IBinding
import io.github.toyota32k.utils.Callback
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.ListenerKey
import io.github.toyota32k.utils.Listeners

class ClickBinding<V> (
        owner:LifecycleOwner,
        val view: V,
        val listeners :Listeners<View>,
        fn:((View)->Unit),
) : IBinding, View.OnClickListener where V:View {
    constructor(owner:LifecycleOwner, view:V, fn:((View)->Unit)) : this(owner,view,Listeners<View>(), fn)
    override val mode: BindingMode = BindingMode.OneWayToSource
    var key: IDisposable? = null
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