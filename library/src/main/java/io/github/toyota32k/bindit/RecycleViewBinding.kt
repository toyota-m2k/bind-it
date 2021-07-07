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
) : IBinding {

    override val mode: BindingMode = BindingMode.OneWay
//    var adapter: Disposable? = null


//    fun connect(owner:LifecycleOwner, view:RecyclerView) {
//        view.adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
//    }

    override fun dispose() {
        val adapter = view.adapter as? IDisposable ?: return
        adapter.dispose()
    }

    override fun isDisposed(): Boolean {
        return (view.adapter as? IDisposable)?.isDisposed() ?: false
    }

    companion object {
        fun <T> create(owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int, bindView:(Binder, View, T)->Unit) : RecycleViewBinding<T> {
            return RecycleViewBinding(list,view).apply {
                view.adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
            }
        }

        // RecyclerView で、layout_height = wrap_content を指定しても、ビューの高さがコンテントの増減に追従しないので、
        // stackoverflow の記事を参考に、adapter を差し替える荒業で乗り切ったつもりでいたが、単に、setHasFixedSize(true) にしていたから、サイズが変更しなかっただけだったことが判明。
        // setHasFixedSize(false)にすれば、RecyclerViewAdapter.Simple で期待通りに動作することを確認した。
//        fun <T> createHeightWrapContent(owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int, bindView:(Binder, View, T)->Unit) : RecycleViewBinding<T> {
//            return RecycleViewBinding(list,view).apply {
//                view.adapter = RecyclerViewAdapter.HeightWrapContent(owner,list,itemViewLayoutId,view,bindView)
//            }
//        }
//        fun <T,B> create(owner: LifecycleOwner, view: RecyclerView, list:ObservableList<T>, createView:(parent: ViewGroup, viewType:Int)->B, bind: (binding: B, item:T)->Unit) : RecycleViewBinding<T>
//        where B: ViewDataBinding {
//            return RecycleViewBinding(list,view).apply {
//                view.adapter = RecyclerViewAdapter.SimpleWithDataBinding<T,B>(owner,list,createView,bind)
//            }
//        }
    }
}