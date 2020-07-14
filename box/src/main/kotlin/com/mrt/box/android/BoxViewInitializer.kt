package com.mrt.box.android

import com.mrt.box.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface BoxViewInitializer {
    fun initializeView(v: BoxAndroidView, vm: Vm?)
    fun onCleared()
}