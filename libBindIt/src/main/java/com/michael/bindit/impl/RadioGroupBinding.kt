package com.michael.bindit.impl

import android.widget.RadioGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.michael.bindit.BindingMode

interface IDValueResolver<T> {
    fun id2value(id:Int) : T?
    fun value2id(v:T): Int
}

@Suppress("unused")
open class RadioGroupBinding<T>(
    owner:LifecycleOwner,
    val view:RadioGroup,
    val mutableData:MutableLiveData<T>,
    val idResolver: IDValueResolver<T>,
    mode:BindingMode = BindingMode.TwoWay
) : BaseBinding<T>(owner,mutableData,mode) {
    private var radioListener:RadioGroup.OnCheckedChangeListener? = null

    init {
        if(mode!=BindingMode.OneWay) {
            radioListener = RadioGroup.OnCheckedChangeListener {group, checkedId ->
                onCheckedChanged(group, checkedId)
            }.apply {
                view.setOnCheckedChangeListener(this)
            }
        }
    }


    override fun cleanup() {
        super.cleanup()
        if(radioListener!=null) {
            radioListener = null
            view.setOnCheckedChangeListener(null)
        }
    }

    override fun onDataChanged(v: T?) {
        if(v!=null) {
            val id = idResolver.value2id(v)
            if(view.checkedRadioButtonId!=id) {
                view.check(id)
            }
        }
    }

    fun onCheckedChanged(@Suppress("UNUSED_PARAMETER") group: RadioGroup?, checkedId: Int) {
        val v = idResolver.id2value(checkedId)
        if(data.value!=v) {
            mutableData.value = v
        }
    }
}