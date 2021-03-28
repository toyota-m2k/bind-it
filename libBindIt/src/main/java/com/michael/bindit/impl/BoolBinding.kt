package com.michael.bindit.impl

import androidx.lifecycle.*
import com.michael.bindit.BindingMode
import com.michael.bindit.BoolConvert
import com.michael.bindit.util.ConvertLiveData

abstract class BoolBinding(
    rawData: LiveData<Boolean>,
    mode:BindingMode,
    boolConvert: BoolConvert
) : BaseBinding<Boolean>(mode) {
    override val data:LiveData<Boolean> = if(boolConvert==BoolConvert.Straight) rawData else ConvertLiveData<Boolean,Boolean>(rawData as MutableLiveData<Boolean>, { it!=true }, {it!=true})
}
