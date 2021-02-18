package com.michael.bindit.impl

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.michael.bindit.BindingMode

open class TextBinding (
    owner:LifecycleOwner,
    val view: TextView,
    data:LiveData<String>,
    mode:BindingMode = BindingMode.OneWay
) : BaseBinding<String>(owner,data,mode) {

    // region Observer (LiveData-->View)

    override fun onDataChanged(v: String?) {
        if(v!=view.text) {
            view.text = v
        }
    }

    // endregion
}

@Suppress("unused")
open class MutableTextBinding (
    owner:LifecycleOwner,
    view: EditText,
    private val mutableData: MutableLiveData<String>,
    mode: BindingMode = BindingMode.TwoWay
) : TextBinding(owner,view,mutableData,mode) {

    inner class Watcher:TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val tx = s?.toString()
            if(tx!=mutableData.value) {
                mutableData.value = tx
            }
        }
    }
    private var watcher:Watcher? = null

    init {
        if(mode!=BindingMode.OneWay) {
            watcher = Watcher()
            view.addTextChangedListener(watcher)
        }
    }

    override fun cleanup() {
        super.dispose()
        val w = watcher ?: return
        view.removeTextChangedListener(w)
    }

    // endregion
}