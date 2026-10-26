package com.kartaviy.tgagent

import android.app.Application
import com.kartaviy.tgagent.tg.Tg

class App : Application() {
    lateinit var prefs: Prefs
        private set
    lateinit var tg: Tg
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        prefs = Prefs(this)
        tg = Tg(this, prefs)
        if (prefs.isConfigured) tg.start()
    }

    companion object {
        lateinit var instance: App
            private set
    }
}
