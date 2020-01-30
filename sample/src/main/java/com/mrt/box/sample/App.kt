package com.mrt.box.sample

import android.app.Application
import android.util.Log
import com.mrt.box.core.Box

/**
 * Created by jaehochoe on 2020-01-03.
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Box.enableLog {
            Log.e("MVCO", it)
        }
    }
}