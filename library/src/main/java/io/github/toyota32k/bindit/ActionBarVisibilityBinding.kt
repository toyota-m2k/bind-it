package io.github.toyota32k.bindit

import android.os.Build
import android.view.View
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import io.github.toyota32k.bindit.*
import io.github.toyota32k.utils.IDisposable
import io.github.toyota32k.utils.LifecycleOwnerHolder

/**
 * ActionBar（アプリのタイトルバー的なやつ）の表示・非表示をビューモデルにバインドするためのクラス
 * @param interlockWithStatusBar trueにすると、ActionBarと共にステータスバー（OSが表示している画面最上部のアレ）の表示・非表示も、dataに連動する
 */
class ActionBarVisibilityBinding(
    data: LiveData<Boolean>,
    val interlockWithStatusBar:Boolean,
    boolConvert: BoolConvert) : BoolBinding(data,BindingMode.OneWay, boolConvert) {
    override fun onDataChanged(v: Boolean?) {
        when(v?:return) {
            true-> {
                showActionBar()
                if(interlockWithStatusBar) {
                    showStatusBar()
                }
            }
            false-> {
                hideActionBar()
                if(interlockWithStatusBar) {
                    hideStatusBar()
                }
            }
        }
    }

    private var activityOwner : LifecycleOwnerHolder? = null
    private val activity: AppCompatActivity? get() = activityOwner?.lifecycleOwner as? AppCompatActivity

    fun attachActivity(activity:AppCompatActivity): IDisposable {
        return LifecycleOwnerHolder(activity) { dispose() }.apply {
            activityOwner = this
        }
    }

    override fun dispose() {
        super.dispose()
        activityOwner?.dispose()
        activityOwner = null
    }

    private fun showActionBar() {
        activity?.supportActionBar?.hide()
    }
    private fun hideActionBar() {
        activity?.supportActionBar?.hide()
    }
    private fun showStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.show(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility =
                (View.SYSTEM_UI_FLAG_IMMERSIVE
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }
    }
}