package org.kel.mics

import android.app.Application
import android.content.Context

class KelmicsApp: Application() {
    companion object {
        lateinit var appCtx: Context

    }

    override fun onCreate() {
        super.onCreate()
        appCtx = applicationContext
    }
}
