package com.mrt.box.android

import com.mrt.box.core.BoxState
import com.mrt.box.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface BoxRenderer {
    fun renderView(
        v: BoxAndroidView,
        s: BoxState,
        vm: Vm?
    )
}