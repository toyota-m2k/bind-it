package com.michael.bindit.impl

import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.michael.bindit.BindingMode

open class SeekBarBinding(
    data: LiveData<Int>,
    min: LiveData<Int>? = null,
    max: LiveData<Int>? = null,
    mode:BindingMode
) : ProgressBarBinding(data,min,max,mode), SeekBar.OnSeekBarChangeListener {
    private val seekBar:SeekBar?
        get() = view as? SeekBar

    fun connect(owner: LifecycleOwner, view: SeekBar) {
        super.connect(owner,view)
        if(mode!=BindingMode.OneWay) {
            view.setOnSeekBarChangeListener(this)
            onProgressChanged(seekBar, seekBar?.progress?:0, false)
        }
    }

    override fun cleanup() {
        if(mode!=BindingMode.OneWay) {
            seekBar?.setOnSeekBarChangeListener(null)
        }
        super.cleanup()
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        mutableData?.apply {
            if(value != progress) {
                value = progress
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

    companion object {
        fun create(owner: LifecycleOwner, view:SeekBar, data:MutableLiveData<Int>,min:LiveData<Int>?=null, max:LiveData<Int>?=null,mode:BindingMode=BindingMode.TwoWay):SeekBarBinding {
            return SeekBarBinding(data,min,max,mode).apply { connect(owner,view) }
        }
    }
}