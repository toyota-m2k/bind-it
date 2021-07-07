package io.github.toyota32k.bindit

import android.widget.SeekBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.bindit.BindingMode

@Suppress("unused")
open class SeekBarBinding(
    data: LiveData<Int>,
    min: LiveData<Int>? = null,
    max: LiveData<Int>? = null,
    mode: BindingMode
) : ProgressBarBinding(data,min,max,mode), SeekBar.OnSeekBarChangeListener {
    private val seekBar:SeekBar?
        get() = view as? SeekBar

    fun connect(owner: LifecycleOwner, view: SeekBar) {
        super.connect(owner,view)
        if(mode!=BindingMode.OneWay) {
            view.setOnSeekBarChangeListener(this)
            if(mode==BindingMode.OneWayToSource||data.value==null) {
                onProgressChanged(seekBar, seekBar?.progress ?: 0, false)
            }
        }
    }

    override fun dispose() {
        if(mode!=BindingMode.OneWay) {
            seekBar?.setOnSeekBarChangeListener(null)
        }
        super.dispose()
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