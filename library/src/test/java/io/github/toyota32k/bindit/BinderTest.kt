package io.github.toyota32k.bindit

import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import junit.framework.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class BinderTest {
    @Rule
    @JvmField
    val instantExecutorRule : InstantTaskExecutorRule = InstantTaskExecutorRule()

    lateinit var activityController : ActivityController<JustTestActivity>

    fun createActivity(): AppCompatActivity {
        activityController = Robolectric.buildActivity(JustTestActivity::class.java)
        return activityController.create().start().get() as AppCompatActivity
    }
    fun finish() {
        activityController.pause().destroy()
    }

    @Test
    fun oldStyleTest() {
        val activity = createActivity()
        val textView = TextView(activity)
        val textData = MutableLiveData<String>()
        val boolData = MutableLiveData<Boolean>(false)

        val binder = Binder().register(
            TextBinding.create(activity, textView, textData),
            EnableBinding.create(activity, textView, boolData)
        )
        assertTrue(textView.text.isNullOrEmpty())
        assertFalse(textView.isEnabled)
        textData.value = "abc"
        assertEquals("abc", textView.text)
        assertFalse(textView.isEnabled)

        boolData.value = true
        assertTrue(textView.isEnabled)

        finish()

        textData.value = "xyz"
        assertEquals(2, binder.count)       // finish()しても、Bindingデータは残っている。LiveDataのobserverは解除されるので、バインド自体は切れている。
        assertEquals("abc", textView.text)

        binder.dispose()
        assertEquals(0, binder.count)
        assertTrue(binder.disposed)

    }

    @Test
    fun withoutBinderOwnerTest() {
        val activity = createActivity()
        val textView = TextView(activity)
        val textData = MutableLiveData<String>()
        val boolData = MutableLiveData<Boolean>(false)

        val binder = Binder()
            .textBinding(activity, textView, textData)
            .enableBinding(activity, textView, boolData)

        // Binderの書き方が変わるだけで、挙動は oldStyleと同じ
        assertTrue(textView.text.isNullOrEmpty())
        assertFalse(textView.isEnabled)
        textData.value = "abc"
        assertEquals("abc", textView.text)
        assertFalse(textView.isEnabled)

        boolData.value = true
        assertTrue(textView.isEnabled)

        finish()

        textData.value = "xyz"
        assertEquals(2, binder.count)       // finish()しても、Bindingデータは残っている。LiveDataのobserverは解除されるので、バインド自体は切れている。
        assertEquals("abc", textView.text)

        binder.dispose()
        assertEquals(0, binder.count)
        assertTrue(binder.disposed)
    }

    @Test
    fun withOwnerTest() {
        val activity = createActivity()
        val textView = TextView(activity)
        val textData = MutableLiveData<String>()
        val boolData = MutableLiveData<Boolean>(false)

        val binder = Binder()
            .owner(activity)
            .textBinding(textView, textData)
            .enableBinding(textView, boolData)

        // Binderの書き方が変わるだけで、挙動は oldStyleと同じ
        assertTrue(textView.text.isNullOrEmpty())
        assertFalse(textView.isEnabled)
        textData.value = "abc"
        assertEquals("abc", textView.text)
        assertFalse(textView.isEnabled)

        boolData.value = true
        assertTrue(textView.isEnabled)

        finish()

        textData.value = "xyz"
        assertEquals(0, binder.count)       // finish()すると、reset()が呼ばれるはず
        assertFalse(binder.disposed)
        assertEquals("abc", textView.text)

        binder.dispose()
        assertEquals(0, binder.count)
        assertTrue(binder.disposed)

    }
}