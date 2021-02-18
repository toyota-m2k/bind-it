@file:Suppress("unused")

package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.michael.bindit.BindingMode

open class EnableBinding(
    owner: LifecycleOwner,
    val view: View,
    data: LiveData<Boolean>
) : BaseBinding<Boolean>(owner,data, BindingMode.OneWay) {
    override fun onDataChanged(v: Boolean?) {
        val enabled = v==true
        view.isEnabled = enabled
        view.focusable = if(enabled) View.FOCUSABLE_AUTO else View.NOT_FOCUSABLE
        view.isClickable = enabled
        view.isFocusableInTouchMode = enabled
    }
}