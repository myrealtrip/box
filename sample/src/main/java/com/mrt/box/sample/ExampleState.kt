package com.mrt.box.sample

import com.mrt.box.android.BoxRenderingScope
import com.mrt.box.core.BoxState

/**
 * Created by jaehochoe on 2020-01-02.
 */
data class ExampleState(
    override val scope: BoxRenderingScope? = null,
    val count: Int = 0,
    val progress: Int = 0
) : BoxState {

    override fun consumer(): BoxState? {
        return copy(
            scope = null
        )
    }
}