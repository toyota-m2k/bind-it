package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.toyota32k.bindit.BindingMode

@Suppress("unused")
class AlphaBinding(
    override val data:LiveData<Float>
) : BaseBinding<Float>(BindingMode.OneWay) {
    override fun onDataChanged(v: Float?) {
        if(v!=null) {
            view?.apply {
                alpha = v
            }
        }
    }
    companion object {
        fun create(owner: LifecycleOwner, view: View, data:LiveData<Float>):AlphaBinding {
            return AlphaBinding(data).apply { connect(owner,view) }
        }
    }
}