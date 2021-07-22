package io.github.toyota32k.bindit

import android.graphics.drawable.Drawable
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData

open class DrawableBinding<V>(
    override val data: LiveData<Drawable>,
    val apply: (V, Drawable)->Unit
) : BaseBinding<Drawable>(BindingMode.OneWay) where V: View {

    override fun onDataChanged(v: Drawable?) {
        if(v==null) return
        @Suppress("UNCHECKED_CAST")
        (view as? V)?.let {
            apply(it,v)
        }
    }
}

@Suppress("unused")
class BackgroundBinding(data:LiveData<Drawable>) : DrawableBinding<View>(data, { view, dr->view.background = dr}) {
    companion object {
        fun create(owner: LifecycleOwner, view:View, data:LiveData<Drawable>) : BackgroundBinding {
            return BackgroundBinding(data).apply{ connect(owner, view) }
        }
    }
}