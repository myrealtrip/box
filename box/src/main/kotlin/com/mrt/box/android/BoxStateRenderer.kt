package com.mrt.box.android

import com.mrt.box.core.BoxState
import com.mrt.box.core.Vm
import java.lang.IllegalArgumentException

/**
 * Created by jaehochoe on 14/07/2020.
 */
interface BoxStateRenderer<S : BoxState> : BoxRenderer {
    fun render(v: BoxAndroidView, s: S, vm: Vm?)
    override fun renderView(v: BoxAndroidView, s: BoxState, vm: Vm?) {
        (s as? S)?.let {
            render(v, it, vm)
        } ?: {
            throw IllegalArgumentException("Unsupported state.")
        }()
    }
}