package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.michael.bindit.BindingMode
import com.michael.bindit.BoolConvert

@Suppress("unused")
open class VisibilityBinding(
        data: LiveData<Boolean>,
        boolConvert: BoolConvert = BoolConvert.Staright,
        private val hiddenMode:HiddenMode = HiddenMode.HideByGone
) : BoolBinding(data,BindingMode.OneWay, boolConvert) {
    enum class HiddenMode {
        HideByGone,
        HideByInvisible,
    }

    override fun onDataChanged(v: Boolean?) {
        val view = this.view ?: return
        view.visibility = when {
            v==true -> View.VISIBLE
            hiddenMode==HiddenMode.HideByGone -> View.GONE
            else -> View.INVISIBLE
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Staright, hiddenMode:HiddenMode = HiddenMode.HideByGone) : VisibilityBinding {
            return VisibilityBinding(data, boolConvert, hiddenMode).apply { connect(owner, view) }
        }
    }
}