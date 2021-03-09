package com.michael.bindit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import com.michael.bindit.impl.*
import com.michael.bindit.util.combineLatest

class MainActivity : AppCompatActivity() {
    enum class RadioValue(val resId:Int, val mtResId:Int) {
        Radio1(R.id.radio1, R.id.mtRadio1),
        Radio2(R.id.radio2, R.id.mtRadio2),
        Radio3(R.id.radio3, R.id.mtRadio3);

        class IDResolver: IIDValueResolver<RadioValue> {
            override fun id2value(id:Int) : RadioValue? {
                return Companion.valueOf(id)
            }
            override fun value2id(v:RadioValue): Int {
                return v.resId
            }
        }
        class MtIDResolver: IIDValueResolver<RadioValue> {
            override fun id2value(id:Int) : RadioValue? {
                return Companion.mtValueOf(id)
            }
            override fun value2id(v:RadioValue): Int {
                return v.mtResId
            }
        }

        companion object {
            fun valueOf(resId: Int, def: RadioValue = Radio1): RadioValue {
                return values().find { it.resId == resId } ?: def
            }
            fun mtValueOf(resId: Int, def: RadioValue = Radio1): RadioValue {
                return values().find { it.mtResId == resId } ?: def
            }
            val idResolver:IIDValueResolver<RadioValue> by lazy { IDResolver() }
            val mtIdResolver:IIDValueResolver<RadioValue> by lazy { MtIDResolver() }
        }

    }

    enum class ToggleValue(val resId:Int) {
        Toggle1(R.id.toggle1),
        Toggle2(R.id.toggle2),
        Toggle3(R.id.toggle3);

        class IDResolver: IIDValueResolver<ToggleValue> {
            override fun id2value(id:Int) : ToggleValue? {
                return ToggleValue.valueOf(id)
            }
            override fun value2id(v:ToggleValue): Int {
                return v.resId
            }
        }

        companion object {
            fun valueOf(resId: Int, def: ToggleValue = ToggleValue.Toggle1): ToggleValue {
                return ToggleValue.values().find { it.resId == resId } ?: def
            }
            val idResolver:IIDValueResolver<ToggleValue> by lazy { ToggleValue.IDResolver() }
        }
    }

    class MainViewModel : ViewModel() {
        val sliderValue = MutableLiveData<Float>(0f)
        val sliderMin = MutableLiveData<Float>(0f)
        val sliderMax = MutableLiveData<Float>(100f)

        val radioValue = MutableLiveData<RadioValue>(RadioValue.Radio1)
        val toggleValue = MutableLiveData<List<ToggleValue>>()

        val tbState1 = MutableLiveData<Boolean>(false)
        val tbState2 = MutableLiveData<Boolean>(true)
        val tbState3 = MutableLiveData<Boolean>(false)

        companion object {
            fun instance(owner: FragmentActivity): MainViewModel {
                return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory()).get(MainViewModel::class.java)
            }
        }
    }

    inner class Binding(owner:LifecycleOwner, mode:MainViewModel): Binder() {
        val slider:Slider by lazy { findViewById(R.id.slider) }
        val numberText:EditText by lazy { findViewById(R.id.numberText) }
        val radioGroup: RadioGroup by lazy { findViewById(R.id.radioGroup)}
        val radioValue: TextView by lazy { findViewById(R.id.radioValue)}
        val toggleGroupAsRadio:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleGroupAsRadio)}
        val toggleGroup:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleGroup)}
        val toggleValue: TextView by lazy {findViewById(R.id.toggleValue)}
        val toggleButtonGroup:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleButtons)}
        val toggleButtonValue:TextView by lazy {findViewById(R.id.toggleButtonValue)}

        init {
            register(
                SliderBinding.create(owner,slider,model.sliderValue, BindingMode.TwoWay, model.sliderMin,model.sliderMax),
                EditNumberBinding.create(owner,numberText,model.sliderValue,BindingMode.TwoWay),

                RadioGroupBinding.create(owner,radioGroup, model.radioValue, RadioValue.idResolver, BindingMode.TwoWay),
                TextBinding.create(owner, radioValue, model.radioValue.map {it.toString()}),
                MaterialRadioButtonGroupBinding.create(owner,toggleGroupAsRadio,model.radioValue, RadioValue.mtIdResolver),

                MaterialToggleButtonGroupBinding.create(owner,toggleGroup, model.toggleValue, ToggleValue.idResolver),
                TextBinding.create(owner, toggleValue, model.toggleValue.map { list-> list.map { it.toString() }.joinToString(", ") }),

                MaterialToggleButtonsBinding.create(owner, toggleButtonGroup, BindingMode.TwoWay,
                    MaterialToggleButtonsBinding.ButtonAndData(findViewById(R.id.toggleButton1),model.tbState1),
                    MaterialToggleButtonsBinding.ButtonAndData(findViewById(R.id.toggleButton2),model.tbState2),
                    MaterialToggleButtonsBinding.ButtonAndData(findViewById(R.id.toggleButton3),model.tbState3),
                    ),
                TextBinding.create(owner,toggleButtonValue,model.tbState1.combineLatest(model.tbState2,model.tbState3){v1,v2,v3->"${v1},${v2},${v3}"})
            )
        }
    }
    private lateinit var model: MainViewModel
    private lateinit var binding: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = MainViewModel.instance(this)
        binding = Binding(this, model)

    }
}