### Simple View-ViewModel Binding Library
# Bind It 

Android platform has a View - ViewModel binding system called `DataBinding`.
Some programmer came from WPF/UWP world (... it' me) may feel it very poor and mysterious.
View - ViewModel binding in WPF/UWP is straight forward and go as planned in most cases.
But DataBinding in Android is not. Some properties won't be bound, KAPT generates errors beyond my comprehension, the code working yesterday is not working today, ...
I'm tired to use it, and gave up it... but, I want to use some binding mechanism like WPF... I have made it by myself. It's Bind-It!
This library contains several classes for data binding and a little utilities.

## Use Bind-It

Add it in your root build.gradle at the end of repositories:
```
allprojects {
  repositories {
    ...
    maven { url 'https://jitpack.io' }
  }
}
```
Add the dependency
```
dependencies {
  implementation 'com.github.toyota-m2k:android-bindit:Tag'
}
```
## Sample

Sample codes below show you how to bind view model to views.
This activity has 2 controls, text input (EditText) and submit button (Button).
Initially the submit button is disabled because the EditText is empty.
When some characters are input to the EditText, the submit button is enabled.

`MainViewModel.kt`
```kotlin
class MainViewModel : ViewModel() {
    val text = MutableLiveData<String>("")
    val isReady = text.map { !it.isNullOrEmpty() }
    val submit = Command { submit() }
  
    private fun submit() {
        // submit text to server, and so on...
    }
} 
```

`MainActivity.kt`
```kotlin
class MainActivity : AppCompatActivity {
  val viewModel by lazy {
    ViewModelProvider(
      this,
      ViewModelProvider.NewInstanceFactory()
    )[MainViewModel::class.java]
  }
  val binder = Binder()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    binder.register(
      // bind viewModel.text to EditText.text property in TwoWay mode. 
      EditTextBinding.create(viewModel.text, findViewById<EditText>(R.id.text), BindingMode.TwoWay),
      // bind viewModel.isReady to Button.isEnabled property in OneWay mode.
      EnableBinding.create(viewModel.isReady, findViewById<Button>(R.id.submit)),
      // bind viewModel.submit command to Submit button.
      viewModel.submit.bindViewEx(findViewById<Button>(R.id.submit))
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    // This 'reset()' may not be necessary.
    // Observer of lifecycle will automatically dispose bindings.
    binder.reset()
  }
}
```
## Binding Mode

- OneWay
  Binding LiveData (=source) to View.
  Updates the property of view only when the source value changes.

- OneWayToSource
  Binding View to MutableLiveData (=source).
  Updates the source value when the property of view changes.

- TwoWay
  Binding mutual View and MutableLiveData.
  Updates the property of view only when the source value changes, and updates the source value when the property of view changes.



## Binding Classes

Binding class named `*Binding` binds a LiveData as data source with a property of view.
Every binding classes have `create()` APIs to create and initialize them and it is recommended to use them instead of using their constructor.

### Boolean Binding Classes

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|CheckBinding|Boolean|CompoundButtons (CheckBox, Switch, ToggleButton, ...) |isChecked|TwoWay|
|EnableBinding|Boolean|View|isEnabled|OneWay|
|MultiEnableBinding|Boolean|View(s)|isEnabled|OneWay|
|VisibilityBinding|Boolean|View|visibility|OneWay|
|MultiVisibilityBinding|Boolean|View(s)|visibility|OneWay|
|FadeInOutBinding|Boolean|View|visibility with fade in/out effect|OneWay|
|AnimationBinding|Boolean|View|animation effect|OneWay|

Boolean Binding classes accept a BoolConvert argument. If BoolConvert.Invert is set, inverted boolean value of source will be set to (and/or get from) the view.
VisibilityBinding accepts a HiddenMode argument in construction. HiddenMode.HideByGone uses View.GONE, and HiddenMode.HideByInvisible uses View.GONE to hide the view.
MultiEnableBinding and MultiVisibilityBinding bind a boolean source to the property of one or more views in same options. 

### Text Binding Classes

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|TextBinding|String|View|text|OneWay|
|IntBinding|Int|View|text|OneWay|
|LongBinding|Long|View|text|OneWay|
|FloatBinding|Float|View|text|OneWay|
|EditTextBinding|String|EditText|text|TwoWay|
|EditIntBinding|Int|EditText|text|TwoWay|
|EditLongBinding|Long|EditText|text|TwoWay|
|EditFloatBinding|Float|EditText|text|TwoWay|

### Progress/Slider Binding Classes

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|ProgressBarBinding|Int|ProgressBar|progress|OneWay|
||Int|ProgressBar|min|OneWay|
||Int|ProgressBar|max|OneWay|
|SeekBarBinding|Int|SeekBar|value|TwoWay|
||Int|SeekBar|min|OneWay|
||Int|SeekBar|max|OneWay|
|SliderBinding|Float|Slider (Material Components)|value|TwoWay|
||Float|Slider|valueFrom|OneWay|
||Float|Slider|valueTo|OneWay|

These classes bind a numeric source to a value of the view.
And optionally, they can bind min/max parameters to range of the value.

### Radio / Toggle Button Binding Classes

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|RadioGroupBinding|Any (using IIDValueResolver)|RadioGroup|checkedRadioButtonId|TwoWay|
|MaterialRadioButtonGroupBinding|Any (using IIDValueResolver)|MaterialButtonToggleGroup (isSingleSelection=true)|TwoWay|
|MaterialToggleButtonGroupBinding|Any (using IIDValueResolver)|MaterialButtonToggleGroup|checkedButtonIds|TwoWay|
|MaterialToggleButtonsBinding|Boolean(s)|MaterialButtonToggleGroup|checkedButtonIds|TwoWay|

RadioGroupBinding, MaterialRadioButtonGroupBinding and MaterialToggleButtonGroupBinding bind any value type to selection of radio/toggle buttons using IIDValueResolver interface.
IIDValueResolver supplies bi-directional conversion between the value (like a value of enum class typically) and @ResId of the button.
On the other hand MaterialToggleButtonsBinding binds sources (MutableLiveData) and Buttons one by one.  

### ObservableList and RecycleViewBinding

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|RecycleViewBinding|ObservableList|RecycleView|RecyclerViewAdapter|OneWay|

ObservableList and RecycleViewBinding are inspired from ObservableCollection in .NET/XAML.
The contents of RecycleView will be automatically updated whenever the elements of ObservableList is modified. 

### Command

This class provides a mechanism of event listener which can associate to View.OnClickListener and TextView.OnEditorActionListener in IDisposable manner.
It is possible to prepare a Command instance with a handler by it's constructor or bind() method, and associate view to it by connectViewEx() method separately.   
The bind() and connectViewEx() methods return a IDisposable instances and you can dispose() them to revoke corresponding listeners. 

## Utilities

- Callback

- Listeners

- 



