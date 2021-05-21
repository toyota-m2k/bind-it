package io.github.toyota32k.utils

/**
 * rxjava �� Disposable ���g�������Ƃ��v�������ǁArxjava�𓱓�����قǂł��Ȃ������Ȃ̂ŁA�Ǝ���`�ɂ��Ă����B
 */
interface IDisposable {
    fun dispose()
    fun isDisposed():Boolean
}