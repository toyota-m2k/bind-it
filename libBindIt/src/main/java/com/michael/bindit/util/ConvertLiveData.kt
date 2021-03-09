package com.michael.bindit.util

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class ConvertLiveData<R,C>(
    val source: MutableLiveData<R>,
    val convert:(R?)->C?,
    val invert:(C?)->R?)
    : MediatorLiveData<C>() {

    init {
        addSource(source) {
            // Sourceの値が変化したときに呼ばれる
            val c = convert(it)
            if(value!=c) {
                value = c
            }
        }
    }

    private fun rawValueChanged(value:R?):C? {
        val c = convert(value)
        super.setValue(c)
        source.value = value
        return c
    }

    override fun setValue(value: C?) {
        super.setValue(value)
        val r = invert(value)
        if(source.value != r) {
            source.value = r
        }
    }

    override fun postValue(value: C) {
        super.postValue(value)
        source.postValue(invert(value))
    }
}