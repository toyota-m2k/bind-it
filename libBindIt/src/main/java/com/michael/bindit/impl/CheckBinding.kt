@file:Suppress("unused")

package com.michael.bindit.impl

import android.widget.CompoundButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.michael.bindit.BindingMode

open class CheckBinding(
    owner: LifecycleOwner,
    val view: CompoundButton,
    data: LiveData<Boolean>,
    mode: BindingMode = BindingMode.OneWay
) : BaseBinding<Boolean>(owner,data,mode) {

    override fun onDataChanged(v: Boolean?) {
        view.isChecked = v == true
    }
}

class MutableCheckBinding(
    owner:LifecycleOwner,
    view: CompoundButton,
    private val mutableData: MutableLiveData<Boolean>,
    mode: BindingMode = BindingMode.TwoWay
): CheckBinding(owner,view,mutableData,mode), CompoundButton.OnCheckedChangeListener {

    init {
        if(mode!=BindingMode.OneWay) {
            view.setOnCheckedChangeListener(this)
        }
    }

    override fun cleanup() {
        super.dispose()
        if(mode!=BindingMode.OneWay) {
            view.setOnCheckedChangeListener(null)
        }
    }

    // region OnCheckedChangeListener

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if(data.value!=isChecked) {
            mutableData.value = isChecked
        }
    }

    // endregion
}