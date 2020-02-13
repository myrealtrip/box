package com.mrt.box.sample

import com.mrt.box.core.BoxSideEffect

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainSideEffect : BoxSideEffect {
    data class AutoCountUp(val count: Int) : MainSideEffect()
    data class Finish(val action: () -> Unit) : MainSideEffect()
}