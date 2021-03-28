@file:Suppress("unused")

package com.michael.bindit.impl

import android.widget.CompoundButton
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.michael.bindit.BindingMode
import com.michael.bindit.BoolConvert

open class CheckBinding protected constructor(
        rawData: LiveData<Boolean>,
        mode: BindingMode,
        boolConvert: BoolConvert = BoolConvert.Straight
) : BoolBinding(rawData,mode,boolConvert), CompoundButton.OnCheckedChangeListener {
    constructor(data:LiveData<Boolean>, boolConvert: BoolConvert=BoolConvert.Straight) : this(data,BindingMode.OneWay,boolConvert)
    private val compoundButton:CompoundButton?
        get() = view as? CompoundButton

    fun connect(owner: LifecycleOwner, view: CompoundButton) {
        super.connect(owner, view)
        if(mode!=BindingMode.OneWay) {
            view.setOnCheckedChangeListener(this)
            onCheckedChanged(view, view.isChecked)
        }
    }

    override fun cleanup() {
        if(mode!=BindingMode.OneWay) {
            compoundButton?.setOnCheckedChangeListener(null)
        }
        super.cleanup()
    }

    // region OnCheckedChangeListener

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        mutableData?.apply {
            if(value!=isChecked) {
                value = isChecked
            }
        }
    }

    override fun onDataChanged(v: Boolean?) {
        val chk = v == true
        compoundButton?.apply {
            if(isChecked!=chk) {
                isChecked = chk
            }
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: CompoundButton, data: LiveData<Boolean>, boolConvert: BoolConvert=BoolConvert.Straight):CheckBinding {
            return CheckBinding(data, BindingMode.OneWay, boolConvert).apply { connect(owner, view) }
        }
        fun create(owner: LifecycleOwner, view:CompoundButton, data: MutableLiveData<Boolean>, boolConvert: BoolConvert=BoolConvert.Straight, mode: BindingMode=BindingMode.TwoWay):CheckBinding {
            return CheckBinding(data, mode, boolConvert).apply { connect(owner, view) }
        }
    }
}
