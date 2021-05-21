package io.github.toyota32k.bindit

import androidx.lifecycle.*
import io.github.toyota32k.bindit.BindingMode
import io.github.toyota32k.bindit.BoolConvert
import io.github.toyota32k.utils.ConvertLiveData

abstract class BoolBinding(
    rawData: LiveData<Boolean>,
    mode:BindingMode,
    boolConvert: BoolConvert
) : BaseBinding<Boolean>(mode) {
    override val data:LiveData<Boolean> = if(boolConvert==BoolConvert.Straight) rawData else ConvertLiveData<Boolean,Boolean>(rawData as MutableLiveData<Boolean>, { it!=true }, {it!=true})
}
