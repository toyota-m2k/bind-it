package io.github.toyota32k.bindit

import android.widget.RadioGroup
import androidx.annotation.IdRes
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.bindit.BindingMode

interface IIDValueResolver<T> {
    fun id2value(@IdRes id:Int) : T?
    fun value2id(v:T): Int
}

@Suppress("unused")
open class RadioGroupBinding<T> (
    override val data: LiveData<T>,
    mode: BindingMode
) : BaseBinding<T>(mode), RadioGroup.OnCheckedChangeListener {
    constructor(data:LiveData<T>):this(data,BindingMode.OneWay)

    private lateinit var idResolver: IIDValueResolver<T>
    private val radioGroup:RadioGroup?
        get() = view as? RadioGroup

    fun connect(owner: LifecycleOwner, view:RadioGroup, idResolver:IIDValueResolver<T>) {
        this.idResolver = idResolver
        super.connect(owner,view)
        if(mode!=BindingMode.OneWay) {
            view.setOnCheckedChangeListener(this)
            if(mode==BindingMode.OneWayToSource||data.value==null) {
                onCheckedChanged(radioGroup, radioGroup?.checkedRadioButtonId?:-1)
            }
        }
    }

    override fun dispose() {
        radioGroup?.setOnCheckedChangeListener(null)
        super.dispose()
    }

    override fun onDataChanged(v: T?) {
        val view = radioGroup ?: return
        if(v!=null) {
            val id = idResolver.value2id(v)
            if(view.checkedRadioButtonId!=id) {
                view.check(id)
            }
        }
    }

    override fun onCheckedChanged(@Suppress("UNUSED_PARAMETER") group: RadioGroup?, @IdRes checkedId: Int) {
        val v = idResolver.id2value(checkedId)
        mutableData?.apply {
            if (value != v) {
                value = v
            }
        }
    }

    companion object {
        fun <T> create(owner: LifecycleOwner, view: RadioGroup, data: LiveData<T>, idResolver: IIDValueResolver<T>):RadioGroupBinding<T> {
            return RadioGroupBinding(data).apply { connect(owner, view, idResolver) }
        }
        fun <T> create(owner: LifecycleOwner, view: RadioGroup, data: MutableLiveData<T>, idResolver: IIDValueResolver<T>, mode:BindingMode=BindingMode.TwoWay):RadioGroupBinding<T> {
            return RadioGroupBinding(data, mode).apply { connect(owner, view, idResolver) }
        }
    }
}
