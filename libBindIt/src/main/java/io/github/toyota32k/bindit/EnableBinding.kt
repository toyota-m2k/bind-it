@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import io.github.toyota32k.bindit.BindingMode
import io.github.toyota32k.bindit.BoolConvert

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