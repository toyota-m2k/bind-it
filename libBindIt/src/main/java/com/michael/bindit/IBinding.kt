package com.michael.bindit

import io.reactivex.rxjava3.disposables.Disposable

enum class BindingMode {
    OneWay,
    OneWayToSource,
    TwoWay,
}


interface IBinding : Disposable

