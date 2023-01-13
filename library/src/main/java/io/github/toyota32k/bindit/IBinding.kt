package io.github.toyota32k.bindit

import io.github.toyota32k.utils.IDisposable

enum class BindingMode {
    OneWay,
    OneWayToSource,
    TwoWay,
}

enum class BoolConvert {
    Straight,   // true --> true
    Inverse,    // true --> false
}

interface IBinding : IDisposable {
    val mode:BindingMode
}

