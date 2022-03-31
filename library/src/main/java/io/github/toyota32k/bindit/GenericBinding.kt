package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

open class GenericBinding<V,T>(
    override val data: LiveData<T>,
    val action: (V,T?)->Unit
) : BaseBinding<T>(BindingMode.OneWay) where V:View {

    @Suppress("UNCHECKED_CAST")
    override fun onDataChanged(v: T?) {
        val typedView:V = view as? V ?: return
        action(typedView, v)
    }

    companion object {
        fun <V,T> create(owner: LifecycleOwner, view:V, data:LiveData<T>, action:(V,T?)->Unit) : GenericBinding<V,T> where V:View {
            return GenericBinding(data, action).apply { connect(owner,view) }
        }
    }
}