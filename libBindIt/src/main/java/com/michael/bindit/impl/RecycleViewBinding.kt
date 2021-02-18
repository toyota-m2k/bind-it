@file:Suppress("unused")

package com.michael.bindit.impl

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.michael.bindit.Binder
import com.michael.bindit.impl.list.ObservableList
import com.michael.bindit.impl.list.RecyclerViewAdapter

class RecycleViewBinding<T>(
    owner:LifecycleOwner,
    val view:RecyclerView,
    list: ObservableList<T>,
    itemViewLayoutId:Int,
    bindView:(Binder, View, T)->Unit
) : DisposableImpl() {
    @Suppress("MemberVisibilityCanBePrivate")
    val adapter = RecyclerViewAdapter.Simple(owner,list,itemViewLayoutId,bindView)
    init {
        view.adapter = adapter
    }

    override fun cleanup() {
        adapter.dispose()
    }
}