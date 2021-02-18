@file:Suppress("unused")

package com.michael.bindit.impl

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.slider.Slider
import com.michael.bindit.BindingMode

class SliderBinding(
    owner:LifecycleOwner,
    val view: Slider,
    private val mutableData: MutableLiveData<Float>,
    private val min:LiveData<Float>? = null,
    private val max:LiveData<Float>? = null,
    mode:BindingMode = BindingMode.TwoWay
    ) : BaseBinding<Float>(owner, mutableData, mode), Slider.OnChangeListener
{
    private var minObserver: Observer<Float?>? = null
    private var maxObserver: Observer<Float?>? = null
    init {
        if(min!=null) {
            minObserver = Observer<Float?> {
                if(it!=null) {
                    view.valueFrom = it
                }
            }.apply {
                min.observe(owner,this)
            }
        }
        if(max!=null) {
            maxObserver = Observer<Float?> {
                if(it!=null) {
                    view.valueTo = it
                }
            }.apply {
                max.observe(owner,this)
            }
        }
        if(mode!=BindingMode.OneWay) {
            view.addOnChangeListener(this)
        }
    }

    override fun onDataChanged(v: Float?) {
        val t = v ?: 0f
        if(view.value != t) {
            view.value = t
        }
    }

    // Slider.OnChangeListener
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if(fromUser && data.value!=value) {
            mutableData.value = value
        }
    }
}