package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-23.
 */
interface BoxView<S: BoxState, E: BoxEvent> {
    open fun render(state: S)
    open fun intent(event: E)
}