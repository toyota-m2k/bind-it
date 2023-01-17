@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.github.toyota32k.bindit.list.ObservableList
import io.github.toyota32k.bindit.list.RecyclerViewAdapter
import io.github.toyota32k.utils.IDisposable
import kotlinx.coroutines.flow.Flow

class RecyclerViewBinding<T>(
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
        enableDragAndDrop(false)
//        list.dispose()    adapter の リスナーは、adapter.dispose()でクリアされるので、これは不要。むしろ、Binder以外でaddされたリスナーも解除されてしまうのでダメぜったい
        (view.adapter as? IDisposable)?.dispose()
    }

    private var itemTouchHelper: ItemTouchHelper? = null
    fun enableDragAndDrop(sw:Boolean) {
        if(sw) {
            if(itemTouchHelper==null) {
                itemTouchHelper = ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                        val from = viewHolder.bindingAdapterPosition
                        val to = target.bindingAdapterPosition
                        list.move(from, to)
                        return true
                    }

                    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    }
                }).apply { attachToRecyclerView(view) }
            }
        } else {
            itemTouchHelper?.attachToRecyclerView(null)
            itemTouchHelper = null
        }
    }

//    override fun isDisposed(): Boolean {
//        return (view.adapter as? IDisposable)?.isDisposed() ?: false
//    }

    companion object {
        fun <T> create(owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
                       fixedSize:Boolean = true,
                       layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
                       bindView:(Binder, View, T)->Unit) : RecyclerViewBinding<T> {
            return RecyclerViewBinding(list,view).apply {
                view.setHasFixedSize(fixedSize)
                view.layoutManager = layoutManager
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

fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = add(RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView))

fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop:Boolean,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = add(RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView).apply { enableDragAndDrop(dragAndDrop) })


fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: LiveData<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder {
    val b = RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
    return genericBoolBinding(owner, view, dragAndDrop) {_,dd-> b.enableDragAndDrop(dd) }
         .recyclerViewBinding(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
}

fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: Flow<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder {
    val b = RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
    return genericBoolBinding(owner, view, dragAndDrop) { _,dd-> b.enableDragAndDrop(dd) }
        .recyclerViewBinding(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
}

fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop:Boolean,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)

fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: LiveData<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)

fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: Flow<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)
