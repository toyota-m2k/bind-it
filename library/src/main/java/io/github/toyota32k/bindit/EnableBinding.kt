@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.toyota32k.utils.UtLog

import io.github.toyota32k.utils.disposableObserve

open class EnableBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    val alphaOnDisabled:Float = 1f
) : BoolBinding(rawData, BindingMode.OneWay, boolConvert) {
    override fun onDataChanged(v: Boolean?) {
        val view = this.view ?: return
        val enabled = v==true
        view.isEnabled = enabled
        view.isClickable = enabled
        if(alphaOnDisabled<1f) {
            view.alpha = if(enabled) 1f else alphaOnDisabled
        }
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            view.focusable = if(enabled) View.FOCUSABLE_AUTO else View.NOT_FOCUSABLE
//        }
//        view.isFocusableInTouchMode = enabled
    }
    companion object {
        fun create(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, alphaOnDisabled: Float=1f) : EnableBinding {
            return EnableBinding(data, boolConvert, alphaOnDisabled).apply { connect(owner, view) }
        }
    }
}

class MultiEnableBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    alphaOnDisabled:Float = 1f
) : EnableBinding(rawData, boolConvert, alphaOnDisabled) {
    private val views = mutableListOf<View>()

    override fun onDataChanged(v: Boolean?) {
        for(view in views) {
            val enabled = v == true
            view.isEnabled = enabled
            view.isClickable = enabled
            if(alphaOnDisabled<1f) {
                view.alpha = if(enabled) 1f else alphaOnDisabled
            }
        }
    }

    override fun connect(owner: LifecycleOwner, view:View) {
        UtLog.libLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner:LifecycleOwner, vararg targets:View) {
        UtLog.libLogger.assert(mode==BindingMode.OneWay, "MultiVisibilityBinding ... support OneWay mode only.")
        if(observed==null) {
            observed = data.disposableObserve(owner, this::onDataChanged)
        }
        views.addAll(targets)
        onDataChanged(data.value)
    }

    override fun dispose() {
        views.clear()
        super.dispose()
    }

    companion object {
        fun create(owner: LifecycleOwner, vararg views: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, alphaOnDisabled: Float=1f) : MultiEnableBinding {
            return MultiEnableBinding(data, boolConvert, alphaOnDisabled).apply { connectAll(owner, *views) }
        }
    }

}