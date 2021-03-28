package com.michael.bindit.util

/**
 * rxjava の Disposable を使おうかとも思ったけど、rxjavaを導入するほどでもなさそうなので、独自定義にしておく。
 */
interface IDisposable {
    fun dispose()
    fun isDisposed():Boolean
}