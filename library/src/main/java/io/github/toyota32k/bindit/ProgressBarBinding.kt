@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.widget.ProgressBar
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.Flow

/**
 * 注意
 * AndroidのProgressBarには、indeterminateモード（ぐるぐる回ったり、行ったり来たりして、実行中であることを示すだけのモード）と、
 * 進捗率を表示するモード（いわゆるプログレスバー）がある。ProgressBarBindingを使うには、進捗率表示モードにする必要があるが、
 * > ProgressBar#isIntdeterminate = false
 * とするだけではだめで、スタイルに "@android:style/Widget.Holo.Light.ProgressBar.Horizontal" など、進捗表示可能なものを指定しなければならない。
 */
open class ProgressBarBinding protected constructor(
    override val data: LiveData<Int>,
    private val min:LiveData<Int>?,
    private val max:LiveData<Int>?,
    mode: BindingMode
) : BaseBinding<Int>(mode) {
    constructor(data:LiveData<Int>, min:LiveData<Int>?=null,max:LiveData<Int>?=null) :this(data,min,max,BindingMode.OneWay)

    private val progressBar:ProgressBar?
        get() = view as? ProgressBar

    private var minObserver: Observer<Int>? = null
    private var maxObserver: Observer<Int>? = null

    fun connect(owner: LifecycleOwner, view: ProgressBar) {
        view.isIndeterminate = false
        if(min!=null) {
            minObserver = Observer<Int> {
                if(it!=null) {
                    if(view.max < it) {
                        view.max = it
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        view.min = it
                    }
                }
            }.apply {
                min.observe(owner,this)
            }
        }
        if(max!=null) {
            maxObserver = Observer<Int> {
                if(it!=null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        if (view.min > it) {
                            view.min = it
                        }
                    }
                    view.max = it
                }
            }.apply {
                max.observe(owner,this)
            }
        }
        super.connect(owner, view)
    }

    override fun onDataChanged(v: Int?) {
        val view = progressBar ?: return
        val p = v?:0
        if(view.progress!=p) {
            view.progress = p
        }
    }

    companion object {
        fun create(owner: LifecycleOwner, view:ProgressBar, data:LiveData<Int>,min:LiveData<Int>?=null, max:LiveData<Int>?=null):ProgressBarBinding {
            return ProgressBarBinding(data,min,max).apply { connect(owner,view) }
        }
    }
}

fun Binder.progressBarBinding(owner: LifecycleOwner, view:ProgressBar, data:LiveData<Int>,min:LiveData<Int>?=null, max:LiveData<Int>?=null):Binder
        = add(ProgressBarBinding.create(owner,view,data,min,max))
fun Binder.progressBarBinding(owner: LifecycleOwner, view:ProgressBar, data: Flow<Int>, min:LiveData<Int>?=null, max:LiveData<Int>?=null):Binder
        = add(ProgressBarBinding.create(owner,view,data.asLiveData(),min,max))
fun Binder.progressBarBinding(view:ProgressBar, data:LiveData<Int>,min:LiveData<Int>?=null, max:LiveData<Int>?=null):Binder
        = add(ProgressBarBinding.create(requireOwner,view,data,min,max))
fun Binder.progressBarBinding(view:ProgressBar, data: Flow<Int>, min:LiveData<Int>?=null, max:LiveData<Int>?=null):Binder
        = add(ProgressBarBinding.create(requireOwner,view,data.asLiveData(),min,max))
