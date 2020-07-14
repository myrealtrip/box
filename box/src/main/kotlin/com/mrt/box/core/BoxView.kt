package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-23.
 */
interface BoxView {
    open fun render(state: BoxState)
    open fun intent(event: BoxEvent)
}