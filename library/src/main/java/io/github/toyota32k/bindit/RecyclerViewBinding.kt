@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.BaseTransientBottomBar.*
import com.google.android.material.snackbar.Snackbar
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
        // enableDragAndDrop(false)
        dragAndDropHelper?.attachToRecyclerView(null)
        itemTouchHelper?.attachToRecyclerView(null)
//        list.dispose()    adapter の リスナーは、adapter.dispose()でクリアされるので、これは不要。むしろ、Binder以外でaddされたリスナーも解除されてしまうのでダメぜったい
        (view.adapter as? IDisposable)?.dispose()
    }

    private var itemTouchHelper: ItemTouchHelper? = null

    data class UndoParams<T>(
        val buttonLabel:String,            // Snackbar に表示する「Undo」ボタンのラベル
        val itemName:((T)->String),        // T型アイテムのラベルを取得する名前リゾルバ
    )
    data class GestureParams<T>(
        val dragToMove:Boolean,                 // D&D によるアイテムの移動をサポートするか？
        val swipeToDelete:Boolean,              // スワイプによるアイテム削除をサポートするか？
        val enableUndo:UndoParams<T>?,          // アイテム削除の Undo 情報 (nullならUndoなし）, swipeToDelete == false なら無視
        val onItemDeleted:((T)->Unit)?)         // アイテムが削除されたときにコールバックする。listを監視してもよいが、undoされると 削除-->挿入のイベントが発生するのが都合悪い場合にこれを利用

    fun enableGesture(params:GestureParams<T>?) {
        if(params==null) {
            itemTouchHelper?.attachToRecyclerView(null)
            itemTouchHelper = null
            return
        }
        enableGesture(params.dragToMove, params.swipeToDelete, params.enableUndo?.buttonLabel,params.enableUndo?.itemName, params.onItemDeleted)
    }
    fun enableGesture(dragToMove:Boolean, swipeToDelete:Boolean, undoButtonLabel:String?, undoItemName:((T)->String)?, onItemDeleted:((T)->Unit)?) {
        if(dragAndDropHelper!=null) {
            throw IllegalStateException("drag and drop enabled already.")
        }
        itemTouchHelper?.attachToRecyclerView(null)
        itemTouchHelper = null
        if(dragToMove||swipeToDelete) {
            val dragDirs = if(dragToMove) ItemTouchHelper.UP or ItemTouchHelper.DOWN else 0
            val swipeDirs = if(swipeToDelete) ItemTouchHelper.RIGHT else 0
            itemTouchHelper = ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {
                override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                    if(!dragToMove) return false
                    val from = viewHolder.bindingAdapterPosition
                    val to = target.bindingAdapterPosition
                    if(from==to) return false
                    list.move(from, to)
                    return true
                }
                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    if(!swipeToDelete) return
                    val pos = viewHolder.bindingAdapterPosition
                    val item = list[pos]
                    list.removeAt(pos)
                    val label = undoItemName?.invoke(item)
                    if(label==null) {
                        // Undo無効 --> 即通知
                        onItemDeleted?.invoke(item)
                    } else {
                        // Undo有効
                        var undo = false
                        // below line is to display our snackbar with action.
                        Snackbar.make(view, label, Snackbar.LENGTH_LONG).setAction(undoButtonLabel?:"Undo") {
                            undo = true
                            list.add(pos, item)
                        }.addCallback(object: BaseCallback<Snackbar>() {
                            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                if(!undo) {
                                    // Undo用 Snackbarが消えた時点で undo されていなければ呼びだし元に通知
                                    onItemDeleted?.invoke(item)
                                }
                            }
                        }).show()
                    }
                }
            }).apply { attachToRecyclerView(view) }
        }
    }


    private var dragAndDropHelper: ItemTouchHelper? = null
    fun enableDragAndDrop(sw:Boolean) {
        if(itemTouchHelper!=null) {
            throw IllegalStateException("gesture enabled already.")
        }
        if(sw) {
            if(dragAndDropHelper==null) {
                dragAndDropHelper = ItemTouchHelper(object:ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
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
            dragAndDropHelper?.attachToRecyclerView(null)
            dragAndDropHelper = null
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

// region Owner 引数ありコーナー

/**
 * D&D / Gesture サポートなし
 */
fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = add(RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView))

/**
 * D&D サポート指定あり
 */
fun <T> Binder.recyclerViewBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop:Boolean,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = add(RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView).apply { enableDragAndDrop(dragAndDrop) })


/**
 * D&D の動的サポート(LiveData版）
 */
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

/**
 * D&D の動的サポート(Flow版）
 */
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

/**
 * Gesture の静的サポート
 * recyclerViewGestureBinding ...
 * JVMのやつが、dragAndDrop:LiveData<Boolean> と gestureParams: LiveData<RecyclerViewBinding.GestureParams<T>?> を区別できないから名前を変えねばならなかった。
 */
fun <T> Binder.recyclerViewGestureBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: RecyclerViewBinding.GestureParams<T>?,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = add(RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView).apply { enableGesture(gestureParams) })
/**
 * Gesture の動的サポート(LiveData版）
 */
fun <T> Binder.recyclerViewGestureBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: LiveData<RecyclerViewBinding.GestureParams<T>?>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder {
    val b = RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
    return genericBinding(owner, view, gestureParams) {_,p-> b.enableGesture(p) }
        .recyclerViewBinding(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
}
/**
 * Gesture の動的サポート(Flow版）
 */
fun <T> Binder.recyclerViewGestureBinding(
    owner: LifecycleOwner, view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: Flow<RecyclerViewBinding.GestureParams<T>?>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder {
    val b = RecyclerViewBinding.create(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
    return genericBinding(owner, view, gestureParams) {_,p-> b.enableGesture(p) }
        .recyclerViewBinding(owner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
}

// endregion

// region Owner 引数なしコーナー

/**
 * D&Dサポートなし
 */
fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,layoutManager,bindView)
/**
 * D&D サポート指定あり
 */
fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop:Boolean,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)

/**
 * D&D の動的サポート(LiveData版）
 */
fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: LiveData<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)

/**
 * D&D の動的サポート(Flow版）
 */
fun <T> Binder.recyclerViewBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    dragAndDrop: Flow<Boolean>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,dragAndDrop,layoutManager,bindView)

/**
 * Gesture の静的サポート
 */
fun <T> Binder.recyclerViewGestureBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: RecyclerViewBinding.GestureParams<T>?,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewGestureBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,gestureParams,layoutManager,bindView)
/**
 * Gesture の動的サポート(LiveData版）
 */
fun <T> Binder.recyclerViewGestureBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: LiveData<RecyclerViewBinding.GestureParams<T>?>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewGestureBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,gestureParams,layoutManager,bindView)
/**
 * Gesture の動的サポート(Flow版）
 */
fun <T> Binder.recyclerViewGestureBinding(
    view: RecyclerView, list: ObservableList<T>, itemViewLayoutId:Int,
    fixedSize:Boolean = true,
    gestureParams: Flow<RecyclerViewBinding.GestureParams<T>?>,
    layoutManager: RecyclerView.LayoutManager = LinearLayoutManager(view.context),
    bindView:(Binder, View, T)->Unit):Binder
        = recyclerViewGestureBinding(requireOwner,view,list,itemViewLayoutId,fixedSize,gestureParams,layoutManager,bindView)

// endregion