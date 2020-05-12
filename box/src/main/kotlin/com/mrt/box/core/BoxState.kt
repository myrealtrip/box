package com.mrt.box.core


/**
 * Created by jaehochoe on 2019-12-31.
 */
interface BoxState {
    fun consumer() : BoxState {
        return this
    }
}