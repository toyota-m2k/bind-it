@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.UtLog
import io.github.toyota32k.utils.disposableObserve
import kotlinx.coroutines.flow.Flow

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
        UtLog.libLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner:LifecycleOwner, vararg targets:View) : MultiVisibilityBinding {
        UtLog.libLogger.assert(mode==BindingMode.OneWay, "MultiVisibilityBinding ... support OneWay mode only.")
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

class CombinatorialVisibilityBinding(
    owner: LifecycleOwner,
    val data: LiveData<Boolean>,
    ): IBinding {
    override val mode: BindingMode = BindingMode.OneWay
    private var observed: IDisposable? = data.disposableObserve(owner, this::onDataChanged)

    private data class ViewOption(val view:View, val conv:BoolConvert, val hiddenMode: VisibilityBinding.HiddenMode) {
        fun show(flag:Boolean) {
            view.visibility =
                if(flag && conv==BoolConvert.Straight || !flag && conv==BoolConvert.Inverse) {
                    View.VISIBLE
                } else if(hiddenMode == VisibilityBinding.HiddenMode.HideByGone) {
                    View.GONE
                } else {
                    View.INVISIBLE
                }
        }
    }

    private val views = mutableListOf<ViewOption>()

    private fun onDataChanged(value: Boolean?) {
        if(value==null) return
        views.forEach { it.show(value) }
    }

    override fun dispose() {
        observed?.dispose()
        observed = null
    }

    fun straightGone(vararg args:View):CombinatorialVisibilityBinding {
        views.addAll(args.map { ViewOption(it, BoolConvert.Straight, VisibilityBinding.HiddenMode.HideByGone)})
        return this
    }
    fun straightInvisible(vararg args:View):CombinatorialVisibilityBinding {
        views.addAll(args.map { ViewOption(it, BoolConvert.Straight, VisibilityBinding.HiddenMode.HideByInvisible)})
        return this
    }
    fun inverseGone(vararg args:View):CombinatorialVisibilityBinding {
        views.addAll(args.map { ViewOption(it, BoolConvert.Inverse, VisibilityBinding.HiddenMode.HideByGone)})
        return this
    }
    fun inverseInvisible(vararg args:View):CombinatorialVisibilityBinding {
        views.addAll(args.map { ViewOption(it, BoolConvert.Inverse, VisibilityBinding.HiddenMode.HideByInvisible)})
        return this
    }

    companion object {
        fun create(owner: LifecycleOwner, data:LiveData<Boolean>):CombinatorialVisibilityBinding {
            return CombinatorialVisibilityBinding(owner, data)
        }
    }
}
fun Binder.visibilityBinding(owner: LifecycleOwner, view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(VisibilityBinding.create(owner,view,data,boolConvert,hiddenMode))
fun Binder.visibilityBinding(owner: LifecycleOwner, view: View, data: Flow<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(VisibilityBinding.create(owner,view,data.asLiveData(),boolConvert,hiddenMode))

fun Binder.multiVisibilityBinding(owner: LifecycleOwner, vararg views: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(MultiVisibilityBinding.create(owner, views= views, data,boolConvert,hiddenMode))
fun Binder.multiVisibilityBinding(owner: LifecycleOwner, vararg views: View, data: Flow<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(MultiVisibilityBinding.create(owner, views= views, data.asLiveData(),boolConvert,hiddenMode))

fun Binder.combinatorialVisibilityBinding(owner: LifecycleOwner, data:LiveData<Boolean>, fnBindView:(CombinatorialVisibilityBinding)->Unit):Binder
        = add(CombinatorialVisibilityBinding.create(owner,data).apply { fnBindView(this) })
fun Binder.combinatorialVisibilityBinding(owner: LifecycleOwner, data: Flow<Boolean>, fnBindView:(CombinatorialVisibilityBinding)->Unit):Binder
        = add(CombinatorialVisibilityBinding.create(owner,data.asLiveData()).apply { fnBindView(this) })

fun Binder.visibilityBinding(view: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(VisibilityBinding.create(requireOwner,view,data,boolConvert,hiddenMode))
fun Binder.visibilityBinding(view: View, data: Flow<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(VisibilityBinding.create(requireOwner,view,data.asLiveData(),boolConvert,hiddenMode))

fun Binder.multiVisibilityBinding(vararg views: View, data: LiveData<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(MultiVisibilityBinding.create(requireOwner, views= views, data,boolConvert,hiddenMode))
fun Binder.multiVisibilityBinding(vararg views: View, data: Flow<Boolean>, boolConvert: BoolConvert = BoolConvert.Straight, hiddenMode: VisibilityBinding.HiddenMode = VisibilityBinding.HiddenMode.HideByGone):Binder
        = add(MultiVisibilityBinding.create(requireOwner, views= views, data.asLiveData(),boolConvert,hiddenMode))

fun Binder.combinatorialVisibilityBinding(data:LiveData<Boolean>, fnBindView:(CombinatorialVisibilityBinding)->Unit):Binder
        = add(CombinatorialVisibilityBinding.create(requireOwner,data).apply { fnBindView(this) })
fun Binder.combinatorialVisibilityBinding(data: Flow<Boolean>, fnBindView:(CombinatorialVisibilityBinding)->Unit):Binder
        = add(CombinatorialVisibilityBinding.create(requireOwner,data.asLiveData()).apply { fnBindView(this) })