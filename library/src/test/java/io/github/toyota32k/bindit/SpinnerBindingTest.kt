package io.github.toyota32k.bindit

import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import io.github.toyota32k.bindit.list.ListViewAdapter
import junit.framework.TestCase.assertTrue
import org.junit.Rule
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController

@RunWith(RobolectricTestRunner::class)
class SpinnerBindingTest {
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

    fun selectionBindingTest() {
//        val activity = createActivity()
//        val view = Spinner(activity)
//        val data = MutableLiveData<String>("a")
//        val adapter = ListViewAdapter<String>(listOf("a","b","c"), 0) {_,_,_-> }
//        val binding = SpinnerBinding(view, adapter)
//        val sbinding = SpinnerSelectionBinding<String>(data, BindingMode.TwoWay)
        assertTrue(true)
    }
}