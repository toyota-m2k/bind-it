@file:Suppress("unused")

package com.michael.bindit.impl

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.slider.Slider
import com.michael.bindit.BindingMode

open class SliderBinding protected constructor(
    override val data: LiveData<Float>,
    private val min:LiveData<Float>? = null,
    private val max:LiveData<Float>? = null,
    mode:BindingMode
) : BaseBinding<Float>(mode) {
    constructor(data:LiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null) : this(data, min,max, BindingMode.OneWay)

    val slider: Slider?
        get() = view as? Slider

    private var minObserver: Observer<Float?>? = null
    private var maxObserver: Observer<Float?>? = null

    open fun connect(owner: LifecycleOwner, view:Slider) {
        super.connect(owner, view)
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
    }

    override fun cleanup() {
        minObserver?.let {
            min?.removeObserver(it)
            minObserver = null
        }
        maxObserver?.let {
            max?.removeObserver(it)
            maxObserver = null
        }
        super.cleanup()
    }

    override fun onDataChanged(v: Float?) {
        val view = slider ?: return
        val t = v ?: 0f
        if(view.value != t) {
            view.value = t
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: Slider, data:LiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null) : SliderBinding {
            return SliderBinding(data,min,max,BindingMode.OneWay).apply { connect(owner,view) }
        }
        fun create(owner: LifecycleOwner, view: Slider, data:MutableLiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null, mode: BindingMode=BindingMode.TwoWay) : SliderBinding {
            return MutableSliderBinding.create(owner,view,data,min,max,mode)
        }
    }
}

open class MutableSliderBinding(
    override val data: MutableLiveData<Float>,
    min:LiveData<Float>? = null,
    max:LiveData<Float>? = null,
    mode:BindingMode = BindingMode.TwoWay
    ) : SliderBinding(data,min, max, mode), Slider.OnChangeListener
{

    override fun connect(owner: LifecycleOwner, view:Slider) {
        super.connect(owner, view)
        if(mode!=BindingMode.OneWay) {
            view.addOnChangeListener(this)
            onValueChange(view, view.value, false)
        }
    }

    override fun cleanup() {
        slider?.removeOnChangeListener(this)
        super.cleanup()
    }


    // Slider.OnChangeListener
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if(data.value!=value) {
            data.value = value
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: Slider, data:MutableLiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null, mode: BindingMode=BindingMode.TwoWay) : MutableSliderBinding {
            return MutableSliderBinding(data,min,max,mode).apply { connect(owner,view) }
        }
    }
}