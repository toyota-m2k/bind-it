@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.toyota32k.utils.UtLogger
import io.github.toyota32k.utils.disposableObserve

open class EnableBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight
) : BoolBinding(rawData, BindingMode.OneWay, boolConvert) {
    override fun onDataChanged(v: Boolean?) {
        val view = this.view ?: return
        val enabled = v==true
        view.isEnabled = enabled
        view.isClickable = enabled
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            view.focusable = if(enabled) View.FOCUSABLE_AUTO else View.NOT_FOCUSABLE
//        }
//        view.isFocusableInTouchMode = enabled
    }
    companion object {
        fun create(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight) : EnableBinding {
            return EnableBinding(data, boolConvert).apply { connect(owner, view) }
        }
    }
}

class MultiEnableBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight
) : EnableBinding(rawData, boolConvert) {
    private val views = mutableListOf<View>()

    override fun onDataChanged(v: Boolean?) {
        for(view in views) {
            val enabled = v == true
            view.isEnabled = enabled
            view.isClickable = enabled
        }
    }

    override fun connect(owner: LifecycleOwner, view:View) {
        UtLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner:LifecycleOwner, vararg targets:View) {
        UtLogger.assert(mode==BindingMode.OneWay, "MultiVisibilityBinding ... support OneWay mode only.")
        if(observed==null) {
            observed = data.disposableObserve(owner, this::onDataChanged)
        }
        views.addAll(targets)
        if(data.value!=null) {
            onDataChanged(data.value)
        }
    }

    override fun dispose() {
        views.clear()
        super.dispose()
    }

    companion object {
        fun create(owner: LifecycleOwner, vararg views: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight) : MultiEnableBinding {
            return MultiEnableBinding(data, boolConvert).apply { connectAll(owner, *views) }
        }
    }

}