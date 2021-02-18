package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.michael.bindit.BindingMode

@Suppress("unused")
open class VisibilityBinding(
    owner:LifecycleOwner,
    val view: View,
    data: LiveData<Boolean>,
    private val hiddenMode:HiddenMode = HiddenMode.HideByGone
) : BaseBinding<Boolean>(owner,data,BindingMode.OneWay) {
    enum class HiddenMode {
        HideByGone,
        HideByInvisible,
    }

    override fun onDataChanged(v: Boolean?) {
        view.visibility = when {
            v==true -> View.VISIBLE
            hiddenMode==HiddenMode.HideByGone -> View.GONE
            else -> View.INVISIBLE
        }
    }
}