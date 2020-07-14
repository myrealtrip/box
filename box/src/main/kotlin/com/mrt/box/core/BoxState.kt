package com.mrt.box.core

import com.mrt.box.android.BoxRenderingScope
import com.mrt.box.android.BoxVoidRenderingScope

/**
 * Created by jaehochoe on 2019-12-31.
 */
interface BoxState {
    val scope: BoxRenderingScope?
    fun consumer(): BoxState? {
        return null
    }

    fun scope(): BoxRenderingScope? {
        return scope ?: BoxVoidRenderingScope
    }
}