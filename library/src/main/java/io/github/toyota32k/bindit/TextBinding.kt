@file:Suppress("unused")

package io.github.toyota32k.bindit

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.bindit.BindingMode

open class TextBinding protected constructor(
    override val data:LiveData<String>,
    mode: BindingMode
) : BaseBinding<String>(mode) {
    constructor(data:LiveData<String>):this(data,BindingMode.OneWay)
    // region Observer (LiveData-->View)

    protected val textView:TextView?
        get() = view as TextView?

    fun connect(owner: LifecycleOwner, view: TextView) {
        super.connect(owner,view)
    }

    override fun onDataChanged(v: String?) {
        val view = textView?:return
        if(v!=view.text.toString()) {
            view.text = v
        }
    }

    // endregion

    companion object {
        fun create(owner:LifecycleOwner, view:TextView, data:LiveData<String>) : TextBinding {
            return TextBinding(data, BindingMode.OneWay).apply { connect(owner,view) }
        }
    }
}

open class EditTextBinding(
    data: MutableLiveData<String>,
    mode: BindingMode
) : TextBinding(data,mode), TextWatcher {

    private val editText:EditText?
        get() = view as EditText?


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    override fun afterTextChanged(s: Editable?) {
        onViewValueChanged(s?.toString())
    }

    open fun onViewValueChanged(tx:String?) {
        mutableData?.apply {
            if(tx!=value) {
                value = tx
            }
        }
    }

    fun connect(owner: LifecycleOwner, view:EditText) {
        super.connect(owner,view)
        if(mode!=BindingMode.OneWay) {
            view.addTextChangedListener(this)
            if(mode==BindingMode.OneWayToSource||data.value==null) {
                afterTextChanged(view.text)
            }
        }
    }

    override fun dispose() {
        editText?.removeTextChangedListener(this)
        super.dispose()
    }

    // endregion
    companion object {
        fun create(owner:LifecycleOwner, view:EditText, data:MutableLiveData<String>, mode:BindingMode=BindingMode.TwoWay):EditTextBinding {
            return EditTextBinding(data,mode).apply { connect(owner,view) }
        }
    }
}