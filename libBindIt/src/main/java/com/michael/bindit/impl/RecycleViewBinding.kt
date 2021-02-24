@file:Suppress("unused")

package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.michael.bindit.Binder
import com.michael.bindit.BindingMode
import com.michael.bindit.impl.list.ObservableList
import com.michael.bindit.impl.list.RecyclerViewAdapter
import io.reactivex.rxjava3.disposables.Disposable

class RecycleViewBinding<T>(
        private val list: ObservableList<T>,
        private val itemViewLayoutId:Int,
        private val bindView:(Binder, View, T)->Unit
) : DisposableImpl() {

    override val mode: BindingMode = BindingMode.OneWay
    var view:RecyclerView? = null
    var adapter: Disposable? = null

    fun connect(owner:LifecycleOwner, view:RecyclerView) {
        view.adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
    }

    override fun cleanup() {
        val adapter = view?.adapter as? Disposable ?: return
        adapter.dispose()
    }

    companion object {
        fun <T> create(owner: LifecycleOwner, view: RecyclerView, list:ObservableList<T>, itemViewLayoutId:Int, bindView:(Binder, View, T)->Unit) : RecycleViewBinding<T> {
            return RecycleViewBinding(list,itemViewLayoutId,bindView).apply { connect(owner,view) }
        }
    }
}