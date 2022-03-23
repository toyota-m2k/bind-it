### Simple View-ViewModel Binding Library
# Bind It 

Android には、Viewのプロパティやイベントと、ViewModel（のLiveData型プロパティ）をバインドする、DataBinding という仕組みがあります。

WPF/UWP の XAMLでは、ごく当たり前の仕組みですが、でも、実際に使ってみると、
思ったより多くの制約があり、頻繁にコンパイルエラーやランタイムエラーが発生します。しかも、エラーメッセージが不親切で、原因にたどり着くのが非常に困難です。KAPTがエラーになると、デバッグすらできず、どこがエラーなのか、そもそも本当にエラーなのか、と何も信じられなくなります。いろいろググって、試行錯誤して、散々苦労して、得られる結果が、EditTextのtextとButtonのonClick程度、ちょっとややこしいバインディングは、Adapterを自分で書かないといけない、とくれば、もう DataBinding は結構です、となります。

このライブラリは、DataBinding を使わずに、Viewのプロパティやイベントと、ViewModel（のLiveData型プロパティ）をバインドするための、シンプル且つ、デバッグ可能な仕掛けを提供するもので、次のような人にお勧めです。

- DataBinding ですっかり疲弊した。
- DataBindingは懲り懲りだが、ViewModelとバインドはしたい。
- アノテーションで生成されるコードや KAPT は信用できない。
- 書きやすさより、デバッグしやすさが大事。
- どうやって動いているかが見えないと安心できない。
  
## Bind-It を使う

`build.gradle`
```
implementation "com.github.toyota-m2k:bind-it:1.X.X"
```

## Bindingクラス

```
IDisposable
    |
    +-- IBinding
        |
        +-- BindingBase
            |
            +-- AlphaBinding
            +-- DrawableBinding
            +-- BoolBinding
            |   |
            |   +-- CheckBinding
            |   +-- EnableBinding
            |   +-- VisibilityBinding
            |   +-- FadeInOutBinding
            |   +-- AnimationBinding
            |
            +-- TextBinding
            +-- EditTextBinding
            +-- NumberBinding
            |   |
            |   +-- IntBinding
            |   +-- LongBinding
            |   +-- FloatBinding
            |
            +-- EditNumberBinding
            |   |
            |   +-- EditIntBinding
            |   +-- EditLongBinding
            |   +-- EditFloatBinding
            |
            +-- ProgressBarBinding
            |   |
            |   +-- SeekBarBinding
            +-- SliderBinding
            +-- RadioGroupBinding
            +-- MaterialToggleButtonsBinding
            +-- MaterialButtonGroupBindingBase
                |
                +-- MaterialRadioButtonGroupBinding
                +-- MaterialToggleButtonGroupBinding
```
Bindingクラスは、

### ■ AlphaBinding

    LiveData<Float> と View.alpha をバインドする。




