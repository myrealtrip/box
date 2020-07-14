package com.mrt.box.core

import com.mrt.box.android.BoxRenderingScope
import com.mrt.box.android.BoxVoidRenderingScope

/**
 * Created by jaehochoe on 2019-12-31.
 */
interface BoxState {
    var scope: BoxRenderingScope?

    fun recycle(): BoxState? {
        return null
    }

    fun scope(): BoxRenderingScope? {
        return scope ?: BoxVoidRenderingScope
    }
}