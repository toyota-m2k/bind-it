### Simple View-ViewModel Binding Library
# Bind It 

Android platform has a View - ViewModel binding system called `DataBinding`.
Some programmers came from WPF/UWP world (... it' me) may feel it very poor and mysterious.
View - ViewModel binding in WPF/UWP is straight forward and go as planned in most cases.
But DataBinding in Android is not. Some properties won't be bound, KAPT generates errors beyond my comprehension, the code working yesterday does not work today, ...
I'm tired to use it, and gave up it... but, I want to use some binding mechanism like WPF... I have made it by myself.
This library contains several classes for data-view bindings and utilities for them.

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

Sample codes below show you how to bind a view model to views.
This activity has 2 controls, text input (EditText) and submit button (Button).
Initially the submit button is disabled because the EditText is empty.
When some characters are input to the EditText, the submit button is enabled.

### Old Style (Ver. 1.x ～)

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
    ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())[MainViewModel::class.java]
  }
  val binder = Binder()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    binder.register(
      // bind viewModel.text to EditText.text property in TwoWay mode. 
      EditTextBinding.create(this, viewModel.text, findViewById<EditText>(R.id.text), BindingMode.TwoWay),
      // bind viewModel.isReady to Button.isEnabled property in OneWay mode.
      EnableBinding.create(this, viewModel.isReady, findViewById<Button>(R.id.submit)),
      // bind viewModel.submit command to Submit button.
      viewModel.submit.attachView(findViewById<Button>(R.id.submit))
    )
  }

  override fun onDestroy() {
    super.onDestroy()
    // This 'reset()' may not be necessary because
    // observer of lifecycle will automatically dispose bindings.
    // But it should be called for cleanup of disposables.
    binder.reset()
  }
}
```
### New Style (Ver. 2.x～)
`MainViewModel.kt`
```kotlin
class MainViewModel : ViewModel() {
    val text = MutableLiveData<String>("")
    val isReady = text.map { !it.isNullOrEmpty() }
    val submit = LiteUnitCommand(this::submit)
  
    private fun submit() {
        // submit text to server, and so on...
    }
} 
```

```kotlin
class MainActivity : AppCompatActivity {
  val viewModel by lazy {
    ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())[MainViewModel::class.java]
  }
  val binder = Binder()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    binder
      // set this activity as lifecycle owner to the binder.      
      .owner(this)
      // bind viewModel.text to EditText#text property in TwoWay mode. 
      .editTextBinding(viewModel.text, findViewById<EditText>(R.id.text), BindingMode.TwoWay)
      // bind viewModel.isReady to Button#isEnabled property in OneWay mode.
      .enableBinding(viewModel.isReady, findViewById<Button>(R.id.submit))
      // bind viewModel.submit command to Submit button.
      .bindCommand(viewModel.submit, findViewById<Button>(R.id.submit))
  }

  override fun onDestroy() {
    super.onDestroy()
    // `binder.reset()` is no more required.
    // Binding objects (=disposables) are automatically disposed when the LifecycleOwner is destructed.
    // binder.reset()
  }
}
```


## Binding Mode

- OneWay
  Binding `LiveData` (=source) to `View`.
  Updates the property of view only when the source value changes.

- OneWayToSource
  Binding `View` to `MutableLiveData` (=source).
  Updates the source value when the property of view changes.

- TwoWay
  Binding mutual `View` and `MutableLiveData`.
  Updates the property of view only when the source value changes, and updates the source value when the property of view changes.

## Binding Classes

Binding class named `*Binding` binds a `LiveData` as data source with a property of view.
And these bindings are automatically revoked when the lifecycle owner is destroyed.
Every binding classes have `create()` helper APIs to create and initialize them.
Furthermore, extend functions of the Binder class to create the binding classes are available in ver.2.x. 
For example, `Binding#visibilityBinding()` creates a VisibilityBinding instance and manage its disposition with lifecycle.

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

Boolean Binding classes (inherited from `BoolBinding` abstract class) accept a `BoolConvert` argument. If `BoolConvert.Inverse` is set, inverted boolean value of source will be set to (and/or get from) the view.
`VisibilityBinding` accepts a `HiddenMode` argument in its construction. `HiddenMode.HideByGone` uses `View.GONE`, and `HiddenMode.HideByInvisible` uses `View.INVISIBLE` to hide the view.
`MultiEnableBinding` and `MultiVisibilityBinding` bind a boolean source to the property of one or more views in same options. 

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

`TextBinding` class binds a String source to a text property of the View in OneWay mode.
`Int/Long/FloatBinding` are inherited from `NumberBinding` and can bind a numeric source to a text property of the View using simple data conversion with `toString()`.
`EditTextBinding` class binds a String source to a text property of the EditText in TwoWay mode.
`EditInt/Long/FloatBinding` can bind numeric sources in TwoWay mode. `String.toInt/Long/FloatOrNull()` extension function are used for reverse conversion.
If you need more complex text formatting rule, you can create a Binding class inherit from `TextBinding` and write converter (and reverse converters) to do so.  

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

`ProgressBarBinding` binds a integer source to a value of the ProgressBar in OneWay mode.
`SeekBarBinding` binds a integer source to a value of the SeekBar in TwoWay mode.
`SliderBinding` is similar to `SeekBarBinding` and it binds a float source to the Slider (which comes from Material Components).
And optionally, they can bind min/max parameters to the range property of the view.

### Radio / Toggle Button Binding Classes

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|RadioGroupBinding|Any (using IIDValueResolver)|RadioGroup|checkedRadioButtonId|TwoWay|
|MaterialRadioButtonGroupBinding|Any (using IIDValueResolver)|MaterialButtonToggleGroup (isSingleSelection=true)|TwoWay|
|MaterialToggleButtonGroupBinding|Any (using IIDValueResolver)|MaterialButtonToggleGroup|checkedButtonIds|TwoWay|
|MaterialToggleButtonsBinding|Boolean(s)|MaterialButtonToggleGroup|checkedButtonIds|TwoWay|

`RadioGroupBinding`, `MaterialRadioButtonGroupBinding` and `MaterialToggleButtonGroupBinding` bind any value type to selection of radio/toggle buttons using `IIDValueResolver` interface.
`IIDValueResolver` supplies bi-directional conversion between the value (for example enum class) and @ResId of the button.
On the other hand `MaterialToggleButtonsBinding` binds sources (MutableLiveData) and Buttons one by one.  

### Command

`Command` class provides a mechanism of event listener which can bind to `View.OnClickListener` and/or `TextView.OnEditorActionListener` in IDisposable manner.
It is possible to prepare a `Command` instance with a handler by it's constructor or `bind()` method, and associate view to it by `attachView()` method separately.
The `bind()` and `attachView()` methods return a `IDisposable` instances and you can `dispose()` them to revoke corresponding listeners
though they will be revoked automatically when it's lifecycleOwner is destroyed.

New command classes `LiteCommand` and `ReliableCommand` come with ver.2.x.
`LiteCommand` is similar to `Command`, but it can handle any type of parameter in callback.
And in most cases, no parameter will be required in callback so you can use `LiteUnitCommand`.

```kotlin
// callback of Command always takes view argument.
val command = Command { view:View-> }.attachView(view)
// callback of LiteCommand takes any type of argument by generics.
val liteCommand = LiteCommand<Int> {id:Int-> }.attachView(view,view.id)
// callback of LiteUnitCommand takes no argument.
val liteUnitCommand = LiteUnitCommand {}.attachView(view)
```

In some special case, Command and LiteCommand will not work as intended.
For example, a long term network operation in background thread and
when the operation is over, it will invoke a `LiteCommand`, unfortunately the lifecycle might be destroyed at the time and
the invocation to `LiteCommand` would not work at all.

In these cases, `ReliableCommand` will work as you had expected.
The invocation to `ReliableCommand` in the dead time of lifecycle will be pending until the new listener is bound with a new lifecycle owner.
As a limitation of `ReliableCommand`, only single listener can be bound to a `ReliableCommand`.


### ObservableList and RecyclerViewBinding

|Binding Class|Source Type|View Type|Target Property|Mode|
|---|---|---|---|---|
|RecycleViewBinding|ObservableList|RecycleView|RecyclerViewAdapter|OneWay|

`ObservableList` is inspired from ObservableCollection in .NET/XAML.
`RecyclerViewBinding` class binds a `ObservableList` to a `RecycleView`.
The contents of `RecycleView` will be automatically updated whenever the elements of ObservableList is modified. 
`RecyclerViewBinding` prepares a `Simple` RecyclerView.Adapter instance internally, which manage every chores about RecyclerView, 
and all you have to do is to implement `bindView` as lambda that initialize a item view.   

```kotlin
class VideoActivity : AppCompatActivity {
  data class VideoItem(val title:String, val source: Uri)
  class VideoViewModel {
    val videoSources = ObservableList<VideoItem>()
    val currentSource = MutableStateFlow<VideoItem?>(null)
    fun selectItem(item:VideoItem) {
      currentSource.value = item
    }
  }
  
  val viewModel by lazy { ViewModelProvider(this,ViewModelProvider.NewInstanceFactory())[VideoViewModel::class.java] }
  val binder = Binder()

  override fun onCreate(savedInstanceState: Bundle?) {
    val videoListView = findViewById<RecyclerView>(R.id.video_list_view)    // RecyclerView instance
    binder.owner(this)
      // create a RecycleViewBinding on videoListView, of which item view will be inflated from R.layout.list_item_view.
      .recyclerViewBinding(videoListView, viewModel.videoSources, R.layout.list_item_view) { itemBinder, view, videoItem ->
        // bindView lambda!!
        // itemBinder is Binder instance prepared by RecyclerViewBinding for each list item view.
        itemBinder.owner(activity)
          // show video title on item view.  
          .textBinding(view.findViewById(R.id.video_item_text), ConstantLiveData(videoItem.title))
          // item selection on tapping item view --> call viewModel.selectItem with a videoItem argument.
          .bindCommand(LiteCommand<VideoItem>(), view, videoItem, viewModel::selectItem)
          // let's show check mark at the current playing item
          .checkBinding(view.findViewById<CheckBox>(R.id.check_box), viewModel.currentSource.map { it?.source == videoItem.source })
      }

    // play the selected item    
    viewModel.currentSource.onEach {
      pleyer.setSource(it)
    }.launchIn(lifecyclescope)
  }
}
```

## Using Kotlin Flow

Though our library designed to use with `LiveData`/`MutableLiveData` originally,
we recently prefer to use `Kotlin Flow` (especially StateFlow/MutableStateFlow) rather than LiveData.
`StateFlow` can be convert into LiveData with `Flow.asLiveData()` extension function and can be used in `OneWay` bindings.
And we provide `MutableStateFlow.asMutableLiveData()` extension function to convert `MutableStateFlow` to MutableLiveData for `TwoWay` bindings.
These converters enable you to use Kotlin Flow with our library.

## Binder

In ver.1.x, `Binder` is a simple collection of IDisposable, and dispose all of registered IDisposables with reset() or dispose() methods.
The `Binder` comes from ver.2 is aware of the lifecycle. `Binder#owner()` method associate a `LifecycleOwner` to the Binder and it will be reset() automatically when the lifecycle owner is destructed.
Furthermore various extended functions of `Binder` are available, and which offer a simple way to use binding classes.

## Utilities

- `Callback`

  Registering a single callback which will be revoked when the lifecycle owner is destroyed or dispose() method is invoked explicitly.

- `Listeners`

  Registering multiple callbacks which will be revoked when the lifecycle owner is destroyed or dispose() method is invoked explicitly.

- `UtLog`

  This is an internal logging system to write logs with class and method name.
  You can instantiate UtLog to use this logging system.
  Though it writes logs to LogCat by default, if you want to use other logging system, implement IUtVaLogger interface and put it to the UtLoggerInstance.externalLogger property.



