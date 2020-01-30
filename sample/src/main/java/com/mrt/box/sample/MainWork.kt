package com.mrt.box.sample

import com.mrt.box.core.BoxWork

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainWork : BoxWork {
    data class AutoCountUp(val count: Int) : MainWork()
    data class Finish(val action: () -> Unit) : MainWork()
}