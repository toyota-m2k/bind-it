package com.michael.bindit.impl

import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.michael.bindit.BindingMode

@Suppress("unused")
class ProgressBarBinding (
    owner: LifecycleOwner,
    val view: ProgressBar,      // LinearProgressIndicator, CircularProgressIndicator
    data: LiveData<Int>
) : BaseBinding<Int>(owner,data,BindingMode.OneWay) {

    override fun onDataChanged(v: Int?) {
        val p = v?:0
        if(view.progress!=p) {
            view.progress = p
        }
    }
}


