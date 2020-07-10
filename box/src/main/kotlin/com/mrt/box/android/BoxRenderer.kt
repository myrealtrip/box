package com.mrt.box.android

import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxState
import com.mrt.box.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface BoxRenderer<S : BoxState, E : BoxEvent> {
    fun render(
            v: BoxAndroidView<S, E>,
            s: S,
            vm: Vm?
    ) : Boolean {
        return false
    }
}