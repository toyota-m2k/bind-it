@file:Suppress("unused")

package com.michael.bindit.impl

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.slider.Slider
import com.michael.bindit.BindingMode

/**
 * Material Componentのスライダー
 * SeekBarより高機能だが、
 * 不正な値（範囲外とかStep位置からずれた値とか）を与えると死ぬので、それを回避するための補正を仕込んである。
 * そのため、EditTextと組み合わせたTwoWayバインドだと、なんか不自然な動きになる。
 *
 * min/max を変更するときに、valueが範囲外になると死ぬし、min==max になっても死ぬし、とにかく、そっと使うように。
 */
open class SliderBinding (
    override val data: LiveData<Float>,
    mode:BindingMode,
    private val min:LiveData<Float>? = null,
    private val max:LiveData<Float>? = null,
) : BaseBinding<Float>(mode), Slider.OnChangeListener {
    constructor(data:LiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null) : this(data, BindingMode.OneWay, min,max)

    val slider: Slider?
        get() = view as? Slider

    private var minObserver: Observer<Float?>? = null
    private var maxObserver: Observer<Float?>? = null

    open fun connect(owner: LifecycleOwner, view:Slider) {
        super.connect(owner, view)
        if(max!=null) {
            maxObserver = Observer<Float?> {
                if(it!=null) {
                    view.valueTo = it
                }
            }.apply {
                max.observe(owner,this)
            }
        }
        if(min!=null) {
            minObserver = Observer<Float?> {
                if(it!=null) {
                    view.valueFrom = it
                }
            }.apply {
                min.observe(owner,this)
            }
        }
        if(mode!=BindingMode.OneWay) {
            view.addOnChangeListener(this)
            onValueChange(view, view.value, false)
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
        if(mode!=BindingMode.OneWay) {
            slider?.removeOnChangeListener(this)
        }
        super.cleanup()
    }

    fun clipByRange(a:Float, b:Float, v:Float):Float {
        val min = Math.min(a,b)
        val max = Math.max(a,b)
        return Math.min(Math.max(min,v), max)
    }

    fun fitToStep(v:Float, s:Float):Float {
        return if(s==0f) {
            v
        } else {
            s*Math.round(v/s)
        }
    }

    override fun onDataChanged(v: Float?) {
        val view = slider ?: return
        // SeekBar と違って、範囲外のValueを与えると描画時に例外が出て死ぬ。
        // stetSize!=0 の場合は、step位置からずれた値を入れるとやっぱり死ぬ。
        val t = fitToStep(clipByRange(view.valueFrom,view.valueTo, v ?: 0f),view.stepSize)
        if(view.value != t) {
            view.value = t
        }
    }
    // Slider.OnChangeListener
    override fun onValueChange(slider: Slider, value: Float, fromUser: Boolean) {
        if(data.value!=value) {
            mutableData?.value = value
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: Slider, data:LiveData<Float>, min:LiveData<Float>?=null, max:LiveData<Float>?=null) : SliderBinding {
            return SliderBinding(data, BindingMode.OneWay, min, max).apply { connect(owner, view) }
        }
        fun create(owner: LifecycleOwner, view: Slider, data:MutableLiveData<Float>, mode: BindingMode=BindingMode.TwoWay, min:LiveData<Float>?=null, max:LiveData<Float>?=null) : SliderBinding {
            return SliderBinding(data, mode, min, max).apply { connect(owner, view) }
        }
    }
}
