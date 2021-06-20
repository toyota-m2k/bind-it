@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import io.github.toyota32k.bindit.list.ObservableList
import io.github.toyota32k.bindit.list.RecyclerViewAdapter
import io.github.toyota32k.utils.IDisposable

class RecycleViewBinding<T>(
        val list: ObservableList<T>,
        val view: RecyclerView
//        private val itemViewLayoutId:Int,
//        private val bindView:(Binder, View, T)->Unit
) : DisposableImpl() {

    override val mode: BindingMode = BindingMode.OneWay
//    var adapter: Disposable? = null


//    fun connect(owner:LifecycleOwner, view:RecyclerView) {
//        view.adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
//    }

    override fun cleanup() {
        val adapter = view.adapter as? IDisposable ?: return
        adapter.dispose()
    }

    companion object {
        fun <T> create(owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int, bindView:(Binder, View, T)->Unit) : RecycleViewBinding<T> {
            return RecycleViewBinding(list,view).apply {
                view.adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
            }
        }
        fun <T> createHeightWrapContent(owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int, bindView:(Binder, View, T)->Unit) : RecycleViewBinding<T> {
            return RecycleViewBinding(list,view).apply {
                view.adapter = RecyclerViewAdapter.HeightWrapContent(owner,list,itemViewLayoutId,view,bindView)
            }
        }
//        fun <T,B> create(owner: LifecycleOwner, view: RecyclerView, list:ObservableList<T>, createView:(parent: ViewGroup, viewType:Int)->B, bind: (binding: B, item:T)->Unit) : RecycleViewBinding<T>
//        where B: ViewDataBinding {
//            return RecycleViewBinding(list,view).apply {
//                view.adapter = RecyclerViewAdapter.SimpleWithDataBinding<T,B>(owner,list,createView,bind)
//            }
//        }
    }
}