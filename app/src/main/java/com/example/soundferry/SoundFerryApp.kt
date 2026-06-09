package com.example.soundferry

import android.app.Application
import com.example.soundferry.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class SoundFerryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@SoundFerryApp)
            modules(appModule)
        }
    }
}
