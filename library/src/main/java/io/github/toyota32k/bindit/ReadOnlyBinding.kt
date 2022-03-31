package com.metamoji.lib.utils.binding.impl

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.metamoji.lib.utils.binding.BindingMode
import com.metamoji.lib.utils.binding.BoolConvert

class ReadOnlyBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight)
    : BoolBinding(rawData, BindingMode.TwoWay, boolConvert) {
    override fun onDataChanged(v: Boolean?) {
        val view = this.view as? EditText ?: return
        val writable = v!=true
        view.isClickable = writable
        view.isCursorVisible = writable
        view.isFocusable = writable
        view.isFocusableInTouchMode = writable
        view.inputType =
        if(writable) inputType else EditorInfo.TYPE_NULL
    }

    var inputType:Int = EditorInfo.TYPE_NULL

    override fun connect(owner: LifecycleOwner, view: View) {
        inputType = (view as? EditText)?.inputType ?: EditorInfo.TYPE_NULL
        super.connect(owner, view)

    }

    companion object {
        fun create(owner: LifecycleOwner, view: EditText, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight) : ReadOnlyBinding {
            return ReadOnlyBinding(data, boolConvert).apply { connect(owner, view) }
        }
    }
}