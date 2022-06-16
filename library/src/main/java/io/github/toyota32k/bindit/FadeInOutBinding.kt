package io.github.toyota32k.bindit

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import io.github.toyota32k.utils.UtLog
import io.github.toyota32k.utils.disposableObserve
import kotlin.math.max

abstract class FadeInOutBase (
    data: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    private val animDuration:Long = 500  // ms
) : BoolBinding(data, BindingMode.OneWay, boolConvert), Animator.AnimatorListener {

    private var showing = false
    private val animating get() = animator.isStarted

    private val animator = ValueAnimator.ofFloat(0f,1f)!!.also { a->
        a.duration = animDuration
        a.addUpdateListener(this::updateAlpha)
        a.addListener(this)
    }

    private val targetVisible:Boolean               // data.value
        get() = data.value == true

    protected abstract var alpha:Float                // view.alpha
    protected abstract var  visibility:  Int          // view.visibility

    private fun updateAlpha(a:ValueAnimator) {
        alpha = if(targetVisible) a.animatedValue as Float else 1f - a.animatedValue as Float
    }

    override fun onDataChanged(v: Boolean?) {
        if(v==true) {
            show()
        } else {
            hide()
        }
    }

    private fun show() {
        if(animating) {
            if(showing) return
            animator.cancel()
            animator.duration = calcRewindingDuration(alpha, showing)
        } else {
            if(visibility == View.VISIBLE) return
            alpha = 0f
            visibility = View.VISIBLE
            animator.duration = animDuration
        }
        showing = true
        animator.start()
    }

    private fun hide() {
        if(animating) {
            if(!showing) return
            animator.cancel()
            animator.duration = calcRewindingDuration(alpha, showing)
        } else {
            if(visibility != View.VISIBLE) return
            animator.duration = animDuration
        }
        showing = false
        animator.start()
    }

    // 巻き戻しに必要な時間
    private fun calcRewindingDuration(alpha:Float, showing:Boolean):Long {
        val ar = if(showing) alpha else 1f-alpha
        val d = (animDuration.toFloat()*ar).toLong()
        return max(100L, d)
    }

    override fun onAnimationStart(animation: Animator?) {
    }

    override fun onAnimationEnd(animation: Animator?) {
        if(targetVisible) {
            alpha = 1f
            visibility = View.VISIBLE
        } else {
            visibility = View.INVISIBLE
        }
    }

    override fun onAnimationCancel(animation: Animator?) {
    }

    override fun onAnimationRepeat(animation: Animator?) {
    }
}

@Suppress("unused")
class FadeInOutBinding(
    data: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    animDuration:Long = 500  // ms
    ) : FadeInOutBase(data, boolConvert, animDuration), Animator.AnimatorListener {

    override var alpha: Float
        get() = view?.alpha ?: 0f
        set(value) { view?.alpha = value }

    override var visibility: Int
        get() = view?.visibility ?: View.INVISIBLE
        set(value) { view?.visibility = value }
}

class MultiFadeInOutBinding(
    data: LiveData<Boolean>,
    boolConvert: BoolConvert = BoolConvert.Straight,
    animDuration: Long = 500
) :FadeInOutBase(data, boolConvert, animDuration) {
    private val views = mutableListOf<View>()

    override var alpha: Float
        get() = views.firstOrNull()?.alpha ?: 0f
        set(value) = views.forEach { it.alpha = value }

    override var visibility: Int
        get() = views.firstOrNull()?.visibility ?: View.INVISIBLE
        set(value) = views.forEach { it.visibility = value }

    override fun connect(owner: LifecycleOwner, view:View) {
        UtLog.libLogger.assert( false,"use connectAll() method.")
    }

    fun connectAll(owner: LifecycleOwner, vararg targets:View) : MultiFadeInOutBinding {
        UtLog.libLogger.assert(mode==BindingMode.OneWay, "MultiVisibilityBinding ... support OneWay mode only.")
        if(observed==null) {
            observed = data.disposableObserve(owner, this::onDataChanged)
        }
        views.addAll(targets)
        if(data.value==null) {
            onDataChanged(data.value)
        }
        return this
    }

    override fun dispose() {
        views.clear()
        super.dispose()
    }
}
