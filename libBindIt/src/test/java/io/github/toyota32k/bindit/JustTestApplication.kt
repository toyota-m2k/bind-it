package io.github.toyota32k.bindit

import android.app.Application

class JustTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        setTheme(R.style.AppTheme) //or just R.style.Theme_AppCompat
    }
}