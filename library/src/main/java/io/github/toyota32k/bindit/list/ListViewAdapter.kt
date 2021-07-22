package io.github.toyota32k.bindit.list

import android.database.DataSetObserver
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import io.github.toyota32k.utils.IDisposable

@Suppress("unused")
abstract class ListViewAdapter<T>(private val observableList: ObservableList<T>) : BaseAdapter(), IDisposable, MutableList<T> by observableList {
    constructor() : this(ObservableList())
    private var disposable :IDisposable? = null

    interface IIdHolder {
        val id:Long
    }

    override fun registerDataSetObserver(observer: DataSetObserver?) {
        disposable?.dispose()
        disposable = observableList.addListenerForever {
            notifyDataSetChanged()
        }
        super.registerDataSetObserver(observer)
    }
    override fun unregisterDataSetObserver(observer: DataSetObserver?) {
        disposable?.dispose()
        disposable = null
        super.unregisterDataSetObserver(observer)
    }

    override fun getCount(): Int {
        return observableList.size
    }

    override fun getItem(position: Int): Any {
        return observableList[position]!!
    }

    override fun getItemId(position: Int): Long {
        val v = getItem(position)
        return if(v is IIdHolder) {
            v.id
        } else {
            position.toLong()
        }
    }

    /**
     * アイテムビューを作る
     */
    abstract fun createItemView(parent: ViewGroup?):View

    /**
     * アイテムビューにデータをセットする
     */
    abstract fun updateItemView(itemView:View, position:Int)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemView = convertView ?: createItemView(parent)
        updateItemView(itemView, position)
        return itemView
    }

    override fun isEmpty(): Boolean {
        return observableList.isEmpty()
    }

    override fun dispose() {
        disposable?.let {
            notifyDataSetInvalidated()
            it.dispose()
            disposable = null
        }

    }

    override fun isDisposed(): Boolean {
        return disposable==null
    }
}