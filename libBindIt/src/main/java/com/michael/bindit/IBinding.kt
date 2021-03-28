package com.michael.bindit

import com.michael.bindit.util.IDisposable

enum class BindingMode {
    OneWay,
    OneWayToSource,
    TwoWay,
}

@Suppress("unused")
enum class BoolConvert {
    Straight,   // true --> true
    Inverse,    // true --> false
}

interface IBinding : IDisposable {
    val mode:BindingMode
}

