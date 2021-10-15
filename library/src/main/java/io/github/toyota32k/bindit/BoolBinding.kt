package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.utils.ConvertLiveData
import io.github.toyota32k.utils.UtLogger
import io.github.toyota32k.utils.disposableObserve

abstract class BoolBinding(
    rawData: LiveData<Boolean>,
    mode:BindingMode,
    boolConvert: BoolConvert
) : BaseBinding<Boolean>(mode) {
    override val data:LiveData<Boolean> = if(boolConvert==BoolConvert.Straight) rawData else ConvertLiveData<Boolean,Boolean>(rawData as MutableLiveData<Boolean>, { it!=true }, {it!=true})
}


open class GenericBoolBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    val applyValue:(View,Boolean)->Unit):BoolBinding(rawData, BindingMode.OneWay, boolConvert) {

    override fun onDataChanged(v: Boolean?) {
        if(v!=null) {
            view?.apply {
                applyValue(this, v)
            }
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, applyValue: (View, Boolean) -> Unit) : GenericBoolBinding {
            return GenericBoolBinding(data, boolConvert,applyValue).apply { connect(owner, view) }
        }
    }
}

open class GenericBoolMultiBinding(
    rawData: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    val applyValue:(List<View>,Boolean)->Unit) :BoolBinding(rawData, BindingMode.OneWay, boolConvert) {
    private val views = mutableListOf<View>()

    override fun onDataChanged(v: Boolean?) {
        if(v!=null) {
            applyValue(views, v)
        }
    }

    override fun connect(owner: LifecycleOwner, view:View) {
        UtLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner:LifecycleOwner, vararg targets:View) {
        UtLogger.assert(mode==BindingMode.OneWay, "GenericBoolMultiBinding ... support OneWay mode only.")
        if(observed==null) {
            observed = data.disposableObserve(owner, this::onDataChanged)
        }
        views.addAll(targets)
        if(data.value==null) {
            onDataChanged(data.value)
        }
    }

    override fun dispose() {
        views.clear()
        super.dispose()
    }

    companion object {
        fun create(owner: LifecycleOwner, vararg targets:View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, applyValue:(List<View>,Boolean)->Unit):GenericBoolMultiBinding {
            return GenericBoolMultiBinding(data, boolConvert, applyValue).apply {
                connectAll(owner, *targets)
            }
        }
    }
}
