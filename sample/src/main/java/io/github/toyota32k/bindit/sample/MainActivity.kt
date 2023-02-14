package io.github.toyota32k.bindit.sample

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.slider.Slider
import io.github.toyota32k.bindit.*
import io.github.toyota32k.bindit.anim.ParallelAnimation
import io.github.toyota32k.bindit.anim.ReversibleValueAnimation
import io.github.toyota32k.bindit.anim.SequentialAnimation
import io.github.toyota32k.utils.UtLog
import io.github.toyota32k.utils.combineLatest
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {
    companion object {
        val logger = UtLog("BindIt.Sample", null, "io.github.toyota32k.bindit.")
    }
    enum class RadioValue(val resId:Int, val mtResId:Int) {
        Radio1(R.id.radio1, R.id.mtRadio1),
        Radio2(R.id.radio2, R.id.mtRadio2),
        Radio3(R.id.radio3, R.id.mtRadio3);

        class IDResolver: IIDValueResolver<RadioValue> {
            override fun id2value(id:Int) : RadioValue {
                return valueOf(id)
            }
            override fun value2id(v: RadioValue): Int {
                return v.resId
            }
        }
        class MtIDResolver: IIDValueResolver<RadioValue> {
            override fun id2value(id:Int) : RadioValue {
                return mtValueOf(id)
            }
            override fun value2id(v: RadioValue): Int {
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
            val idResolver: IIDValueResolver<RadioValue> by lazy { IDResolver() }
            val mtIdResolver: IIDValueResolver<RadioValue> by lazy { MtIDResolver() }
        }

    }

    enum class ToggleValue(val resId:Int) {
        Toggle1(R.id.toggle1),
        Toggle2(R.id.toggle2),
        Toggle3(R.id.toggle3);

        class IDResolver: IIDValueResolver<ToggleValue> {
            override fun id2value(id:Int) : ToggleValue {
                return valueOf(id)
            }
            override fun value2id(v: ToggleValue): Int {
                return v.resId
            }
        }

        companion object {
            fun valueOf(resId: Int, def: ToggleValue = Toggle1): ToggleValue {
                return values().find { it.resId == resId } ?: def
            }
            val idResolver: IIDValueResolver<ToggleValue> by lazy { IDResolver() }
        }
    }

    class MainViewModel : ViewModel() {
        val sliderValue = MutableLiveData(0f)
        val sliderMin = MutableLiveData(0f)
        val sliderMax = MutableLiveData(100f)

        val radioValue = MutableLiveData(RadioValue.Radio1)
        val toggleValue = MutableLiveData<List<ToggleValue>>()

        val tbState1 = MutableLiveData(false)
        val tbState2 = MutableLiveData(true)
        val tbState3 = MutableLiveData(false)

        val multiVisible = MutableLiveData(true)

        val commandTextMessage = MutableLiveData<String>()
        val commandTest = LiteUnitCommand(::onCommandTest)

        val liteCommand = LiteCommand<Boolean> {
            logger.info("LiteCommand called.")
        }
        val reliableCommand = ReliableCommand<Boolean> {
            logger.info("ReliableCommand called.")
        }

        val textValue = MutableLiveData("")

        private fun onCommandTest() {
            commandTextMessage.value = "Testing..."
            CoroutineScope(Dispatchers.IO).launch {
                for(i in 10 downTo 0) {
                    delay(1000L)
                    withContext(Dispatchers.Main) {
                        commandTextMessage.value = "Testing...($i)"
                    }
                }
                withContext(Dispatchers.Main) {
                    liteCommand.invoke(true)
                    reliableCommand.invoke(true)
                }
            }
        }

        companion object {
            fun instance(owner: FragmentActivity): MainViewModel {
                logger.debug()
                return ViewModelProvider(owner, ViewModelProvider.NewInstanceFactory())[MainViewModel::class.java]
            }
        }
    }

    inner class Binding(owner:LifecycleOwner, model: MainViewModel): Binder() {
        private val slider:Slider by lazy { findViewById(R.id.slider) }
        private val numberText:EditText by lazy { findViewById(R.id.numberText) }
        private val radioGroup: RadioGroup by lazy { findViewById(R.id.radioGroup)}
        private val radioValue: TextView by lazy { findViewById(R.id.radioValue)}
        private val toggleGroupAsRadio:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleGroupAsRadio)}
        private val toggleGroup:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleGroup)}
        private val toggleValue: TextView by lazy {findViewById(R.id.toggleValue)}
        private val toggleButtonGroup:MaterialButtonToggleGroup by lazy {findViewById(R.id.toggleButtons)}
        private val toggleButtonValue:TextView by lazy {findViewById(R.id.toggleButtonValue)}
        private val multiVisibleCheckBox: CheckBox by lazy { findViewById(R.id.multiVisibleCheckBox) }
        private val internalTestButton: Button by lazy { findViewById(R.id.internal_test_button) }
        private val commandTestButton:Button by lazy {findViewById(R.id.command_test)}
        private val commandTestText:TextView by lazy {findViewById(R.id.command_test_message)}
        private val textInput:EditText by lazy { findViewById(R.id.text_input) }
        private val textOutput:TextView by lazy { findViewById(R.id.text_output) }

        init {
            logger.debug()
            register(
                SliderBinding.create(
                    owner,
                    slider,
                    model.sliderValue,
                    BindingMode.TwoWay,
                    model.sliderMin,
                    model.sliderMax
                ),
                EditNumberBinding.create(owner, numberText, model.sliderValue, BindingMode.TwoWay),

                RadioGroupBinding.create(
                    owner,
                    radioGroup,
                    model.radioValue,
                    RadioValue.idResolver,
                    BindingMode.TwoWay
                ),
                TextBinding.create(owner, radioValue, model.radioValue.map { it.toString() }),
                MaterialRadioButtonGroupBinding.create(
                    owner,
                    toggleGroupAsRadio,
                    model.radioValue,
                    RadioValue.mtIdResolver
                ),

                MaterialToggleButtonGroupBinding.create(
                    owner,
                    toggleGroup,
                    model.toggleValue,
                    ToggleValue.idResolver
                ),
                TextBinding.create(
                    owner,
                    toggleValue,
                    model.toggleValue.map { list ->
                        list.joinToString(", ") { it.toString() }
                    }),

                MaterialToggleButtonsBinding.create(
                    owner, toggleButtonGroup, BindingMode.TwoWay,
                    MaterialToggleButtonsBinding.ButtonAndData(
                        findViewById(R.id.toggleButton1),
                        model.tbState1
                    ),
                    MaterialToggleButtonsBinding.ButtonAndData(
                        findViewById(R.id.toggleButton2),
                        model.tbState2
                    ),
                    MaterialToggleButtonsBinding.ButtonAndData(
                        findViewById(R.id.toggleButton3),
                        model.tbState3
                    ),
                ),
                TextBinding.create(
                    owner,
                    toggleButtonValue,
                    combineLatest(
                        model.tbState1,
                        model.tbState2,
                        model.tbState3
                    ) { v1, v2, v3 -> "${v1},${v2},${v3}" }),

                MultiFadeInOutBinding(model.multiVisible)
                    .connectAll(owner, radioGroup, toggleGroupAsRadio,toggleGroup, toggleButtonGroup),

//                MultiVisibilityBinding(model.multiVisible, BoolConvert.Straight, VisibilityBinding.HiddenMode.HideByInvisible)
//                    .connectAll(owner, radioGroup, toggleGroupAsRadio,toggleGroup, toggleButtonGroup),
                CheckBinding.create(owner, multiVisibleCheckBox, model.multiVisible),
                ClickBinding(owner, internalTestButton) {
                    internalTest()
                },

                TextBinding.create(owner, commandTestText, model.commandTextMessage),
                model.commandTest.attachView(commandTestButton),
                model.liteCommand.bind(owner) {
                    logger.info("liteCommand bound to owner is called")
                },
                model.reliableCommand.bind(owner) {
                    logger.info("reliableCommand bound to owner is called")
                },
                EditTextBinding.create(owner, textInput, model.textValue),
                model.commandTest.attachView(textInput),
                TextBinding.create(owner, textOutput, model.textValue),
            )
        }
    }
    private lateinit var model: MainViewModel
    private lateinit var binding: Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        logger.debug()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        model = MainViewModel.instance(this)
        binding = Binding(this, model)
    }

    override fun onStart() {
        super.onStart()
        logger.debug()
    }

    override fun onResume() {
        super.onResume()
        logger.debug()
    }

    override fun onRestart() {
        super.onRestart()
        logger.debug()
    }

    override fun onStop() {
        super.onStop()
        logger.debug()
    }

    override fun onPause() {
        super.onPause()
        logger.debug()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        logger.debug()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        logger.debug()
    }

    override fun onDestroy() {
        super.onDestroy()
        logger.debug()
    }

    enum class AnimTestTarget(val enabled:Boolean) {
        VALUE_ANIMATION_SIMPLE(false),
        VALUE_ANIMATION_CANCEL(true),
        SEQUENTIAL_SIMPLE(false),
        SEQUENTIAL_CANCEL(false),
        PARALLEL_SIMPLE(false),
        PARALLEL_CANCEL(false),
    }

    fun internalTest() {
        CoroutineScope(Dispatchers.Main).launch {
            val logger = UtLog("XXX", MainActivity.logger)
            if(AnimTestTarget.VALUE_ANIMATION_SIMPLE.enabled) {
                logger.debug("Simple ValueAnimation")
                withContext(Dispatchers.Main) {
                    var aniVal = 0f
                    val ani = ReversibleValueAnimation(1000).onUpdate {
                        aniVal = it * 100
                        logger.debug("value=${aniVal.toInt()}")
                    }
                    logger.debug("animation straight")
                    var r = ani.run(false)
                    logger.debug("animation straight ... done:$r ($aniVal)")
                    logger.debug("animation reverse")
                    r = ani.run(true)
                    logger.debug("animation reverse ... done:$r ($aniVal)")
                }
            }

            if(AnimTestTarget.VALUE_ANIMATION_CANCEL.enabled) {
                logger.debug("ValueAnimation cancel and reverse")
                withContext(Dispatchers.Main) {
                    var aniVal = 0f
                    var sc = 0
                    var ec = 0
                    val ani = ReversibleValueAnimation(1000).onUpdate {
                        aniVal = it * 100
                        logger.debug("xxx value=${aniVal.toInt()}")
                    }.onStart { r ->
                        sc++
                        logger.debug("started($sc) - reverse=$r")
                    }.onEnd { r ->
                        ec++
                        logger.debug("end($ec) - reverse=$r")
                    }
                    launch {
                        logger.debug("animation straight")
                        val r = ani.run(false)
                        logger.debug("animation(1) straight ... done:$r ($aniVal)")
                    }
                    delay(500)
                    logger.debug("animation(2) reverse")
                    val r = ani.run(true)
                    logger.debug("animation reverse ... done:$r ($aniVal)")
                }
            }

            if(AnimTestTarget.SEQUENTIAL_SIMPLE.enabled) {
                logger.debug("Sequential Animation")
                withContext(Dispatchers.Main) {
                    var v1 = 0f
                    var v2 = 0f
                    var v3 = 0f
                    val ani = SequentialAnimation().add(
                        ReversibleValueAnimation(1000).onUpdate {
                            v1 = it * 100
                            logger.debug("v1=${v1.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v2 = it * 100
                            logger.debug("v2=${v2.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v3 = it * 100
                            logger.debug("v3=${v3.toInt()}")
                        },
                    )
                    logger.debug("SequentialAnimation:Straight")
                    ani.run(false)
                    logger.debug("SequentialAnimation:Straight ... end: v1=$v1, v2=$v2, v3=$v3")
                    logger.debug("SequentialAnimation:Reverse")
                    ani.run(true)
                    logger.debug("SequentialAnimation:Reverse ... end: v1=$v1, v2=$v2, v3=$v3")
                }
            }

            if(AnimTestTarget.SEQUENTIAL_CANCEL.enabled) {
                logger.debug("Sequential Animation ... cancel and reverse")
                withContext(Dispatchers.Main) {
                    var v1 = 0f
                    var v2 = 0f
                    var v3 = 0f
                    val ani = SequentialAnimation().add(
                        ReversibleValueAnimation(1000).onUpdate {
                            v1 = it * 100
                            logger.debug("v1=${v1.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v2 = it * 100
                            logger.debug("v2=${v2.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v3 = it * 100
                            logger.debug("v3=${v3.toInt()}")
                        },
                    )
                    launch {
                        logger.debug("SequentialAnimation:Straight")
                        ani.run(false)
                        logger.debug("SequentialAnimation:Straight ... end: v1=$v1, v2=$v2, v3=$v3")
                    }
                    delay(1500)
                    logger.debug("SequentialAnimation:Reverse")
                    ani.run(true)
                    logger.debug("SequentialAnimation:Reverse ... end: v1=$v1, v2=$v2, v3=$v3")
                }
            }



            if(AnimTestTarget.PARALLEL_SIMPLE.enabled) {
                logger.debug("Parallel Animation")
                withContext(Dispatchers.Main) {
                    var v1 = 0f
                    var v2 = 0f
                    var v3 = 0f
                    val ani = ParallelAnimation().add(
                        ReversibleValueAnimation(1000).onUpdate {
                            v1 = it * 100
                            logger.debug("v1=${v1.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v2 = it * 100
                            logger.debug("v2=${v2.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v3 = it * 100
                            logger.debug("v3=${v3.toInt()}")
                        },
                    )
                    logger.debug("ParallelAnimation:Straight")
                    ani.run(false)
                    logger.debug("ParallelAnimation:Straight ... end: v1=$v1, v2=$v2, v3=$v3")
                    logger.debug("ParallelAnimation:Reverse")
                    ani.run(true)
                    logger.debug("ParallelAnimation:Reverse ... end: v1=$v1, v2=$v2, v3=$v3")
                }
            }

            if(AnimTestTarget.PARALLEL_CANCEL.enabled) {
                logger.debug("Parallel Animation ... cancel and reverse")
                withContext(Dispatchers.Main) {
                    var v1 = 0f
                    var v2 = 0f
                    var v3 = 0f
                    val ani = ParallelAnimation().add(
                        ReversibleValueAnimation(1000).onUpdate {
                            v1 = it * 100
                            logger.debug("v1=${v1.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v2 = it * 100
                            logger.debug("v2=${v2.toInt()}")
                        },
                        ReversibleValueAnimation(1000).onUpdate {
                            v3 = it * 100
                            logger.debug("v3=${v3.toInt()}")
                        },
                    )
                    launch {
                        logger.debug("ParallelAnimation:Straight")
                        ani.run(false)
                        logger.debug("ParallelAnimation:Straight ... end: v1=$v1, v2=$v2, v3=$v3")
                    }
                    delay(500)
                    logger.debug("ParallelAnimation:Reverse")
                    ani.run(true)
                    logger.debug("ParallelAnimation:Reverse ... end: v1=$v1, v2=$v2, v3=$v3")
                }
            }
       }

    }
}