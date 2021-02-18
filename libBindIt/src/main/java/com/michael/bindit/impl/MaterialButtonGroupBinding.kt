@file:Suppress("unused")

package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.material.button.MaterialButtonToggleGroup
import com.michael.bindit.BindingMode
import java.lang.ref.WeakReference

abstract class MaterialButtonGroupBindingBase<T,DataType> (
    owner:LifecycleOwner,
    val view:MaterialButtonToggleGroup,
    val mutableData:MutableLiveData<DataType>,
    val idResolver: IDValueResolver<T>,
    mode:BindingMode = BindingMode.TwoWay
)  : BaseBinding<DataType>(owner, mutableData, mode) {
    private var btnListener: MaterialButtonToggleGroup.OnButtonCheckedListener? = null
    init {
        if(mode!=BindingMode.OneWay) {
            btnListener = MaterialButtonToggleGroup.OnButtonCheckedListener { group, id, isChecked->
                onButtonChecked(group,id,isChecked)
            }.apply {
                view.addOnButtonCheckedListener(this)
            }
        }
    }

    abstract fun onButtonChecked(group: MaterialButtonToggleGroup?, checkedId: Int,isChecked: Boolean)

    override fun cleanup() {
        super.cleanup()
        val bl = btnListener?:return
        view.removeOnButtonCheckedListener(bl)
    }
}

/**
 * MaterialButtonToggleGroup を使ったラジオボタングループのバインディング
 * 考え方は RadioGroup と同じだが、i/fが異なるので、クラスは別になる。
 */
class MaterialRadioButtonGroupBinding<T>(
    owner:LifecycleOwner,
    view:MaterialButtonToggleGroup,
    mutableData:MutableLiveData<T>,
    idResolver: IDValueResolver<T>,
    mode:BindingMode = BindingMode.TwoWay
) : MaterialButtonGroupBindingBase<T,T>(owner,view,mutableData,idResolver,mode) {
    init {
        view.isSingleSelection = true
    }

    // View --> Source
    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        if(isChecked) {
            val v = idResolver.id2value(checkedId)
            if(data.value!=v) {
                mutableData.value = v
            }
        }
    }

    // Source --> View
    override fun onDataChanged(v: T?) {
        if(v!=null) {
            val id = idResolver.value2id(v)
            if(view.checkedButtonId != id) {
                view.clearChecked()
                view.check(id)
            }
        }
    }
}

/**
 * MaterialButtonToggleGroup を使ったトグルボタングループのバインディング
 * 各トグルボタンにT型のユニークキー（enumかR.id.xxxなど）が１：１に対応しているとして、そのListで選択状態をバインドする。
 * MaterialButtonToggleGroupを使う場合、トグルボタンとしてButtonを使うため、個々のボタンの選択状態の指定や選択イベントは使えないので。
 */
class MaterialToggleButtonGroupBinding<T>(
    owner:LifecycleOwner,
    view:MaterialButtonToggleGroup,
    mutableData:MutableLiveData<List<T>>,
    idResolver: IDValueResolver<T>,
    mode:BindingMode = BindingMode.TwoWay
) : MaterialButtonGroupBindingBase<T,List<T>>(owner,view,mutableData,idResolver,mode) {

    private val selected = mutableSetOf<T>()
    private var busy = false
    private fun inBusy(fn:()->Unit) {
        if(!busy) {
            busy = true
            try {
                fn()
            } finally {
                busy = false
            }
        }
    }

    override fun onDataChanged(v: List<T>?) {
        inBusy {
            view.clearChecked()
            selected.clear()
            if (!v.isNullOrEmpty()) {
                selected.addAll(v)
                v.forEach {
                    view.check(idResolver.value2id(it))
                }
            }
        }
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        inBusy {
            val v = idResolver.id2value(checkedId) ?: return@inBusy
            if(isChecked) {
                selected.add(v)
            } else {
                selected.remove(v)
            }
            mutableData.value = selected.toList()
        }
    }
}

/**
 * MaterialButtonToggleGroupに支配される、複数のボタンと、その選択状態(LiveData<Boolean>)を個々にバインドするクラス。
 *
 * Usage:
 * 
 * val binding = MaterialToggleButtonsBinding(owner,toggleGroup).apply {
 *               add(button1, viewModel.toggle1)
 *               add(button2, viewModel.toggle2)
 *               ...
 *            }
 */
class MaterialToggleButtonsBinding (
    owner:LifecycleOwner,
    val view:MaterialButtonToggleGroup,
    val mode:BindingMode = BindingMode.TwoWay
) : DisposableImpl(),  MaterialButtonToggleGroup.OnButtonCheckedListener {

    private inner class DataObserver(val button: View, val data:MutableLiveData<Boolean>) : Observer<Boolean> {
        init {
            if (mode != BindingMode.OneWayToSource) {
                owner?.also {
                    data.observe(it,this)
                }
            }
        }

        fun dispose() {
            data.removeObserver(this)
        }

        override fun onChanged(t: Boolean?) {
            val cur = view.checkedButtonIds.contains(button.id)
            if(t==true) {
                if(!cur) {
                    view.check(button.id)
                }
            } else {
                if(cur) {
                    view.uncheck(button.id)
                }
            }
        }
    }

    private val weakOwner = WeakReference(owner)
    private val owner:LifecycleOwner?
        get() = weakOwner.get()
    private val buttons = mutableMapOf<Int,DataObserver>()

    init {
        if(mode!=BindingMode.OneWay) {
            view.addOnButtonCheckedListener(this)
        }
    }

    fun add(button:View, data:MutableLiveData<Boolean>):MaterialToggleButtonsBinding {
        buttons[button.id] = DataObserver(button,data)
        return this
    }

    override fun cleanup() {
        if (mode != BindingMode.OneWayToSource) {
            buttons.forEach { (_, data) ->
                data.dispose()
            }
        }
        buttons.clear()
        if(mode!=BindingMode.OneWay) {
            view.removeOnButtonCheckedListener(this)
        }
    }

    override fun onButtonChecked(
        group: MaterialButtonToggleGroup?,
        checkedId: Int,
        isChecked: Boolean
    ) {
        val v = buttons[checkedId] ?: return
        if(v.data.value != isChecked) {
            v.data.value = isChecked
        }
    }
}