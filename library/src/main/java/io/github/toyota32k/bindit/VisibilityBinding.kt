package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.toyota32k.utils.UtLogger
import io.github.toyota32k.utils.disposableObserve

@Suppress("unused")
open class VisibilityBinding(
        data: LiveData<Boolean>,
        boolConvert: BoolConvert = BoolConvert.Straight,
        protected val hiddenMode:HiddenMode = HiddenMode.HideByGone
) : BoolBinding(data, BindingMode.OneWay, boolConvert) {
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
        fun create(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode:HiddenMode = HiddenMode.HideByGone) : VisibilityBinding {
            return VisibilityBinding(data, boolConvert, hiddenMode).apply { connect(owner, view) }
        }
    }
}

@Suppress("unused")
class MultiVisibilityBinding(
    data: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    hiddenMode: HiddenMode = HiddenMode.HideByGone
) : VisibilityBinding(data, boolConvert, hiddenMode) {
    private val views = mutableListOf<View>()

    override fun onDataChanged(v: Boolean?) {
        for(view in views) {
            view.visibility = when {
                v == true -> View.VISIBLE
                hiddenMode == HiddenMode.HideByGone -> View.GONE
                else -> View.INVISIBLE
            }
        }
    }

    override fun connect(owner: LifecycleOwner, view:View) {
        UtLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner:LifecycleOwner, vararg targets:View) : MultiVisibilityBinding {
        UtLogger.assert(mode==BindingMode.OneWay, "MultiVisibilityBinding ... support OneWay mode only.")
        if(observed==null) {
            observed = data.disposableObserve(owner, this::onDataChanged)
        }
        views.addAll(targets)
        onDataChanged(data.value)
        return this
    }

    override fun dispose() {
        views.clear()
        super.dispose()
    }

    companion object {
        fun create(owner: LifecycleOwner, vararg views: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode:HiddenMode = HiddenMode.HideByGone) : MultiVisibilityBinding {
            return MultiVisibilityBinding(data, boolConvert,hiddenMode).apply { connectAll(owner, *views) }
        }
    }

}