package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-01.
 */
sealed class BoxOutput<out S : BoxState, out E : BoxEvent, out W : BoxWork> {
    abstract val from: S
    abstract val event: E

    data class Valid<out S : BoxState, out E : BoxEvent, out W : BoxWork>(
        override val from: S,
        override val event: E,
        val to: S,
        val work: W
    ) : BoxOutput<S, E, W>()

    data class Void<out S : BoxState, out E : BoxEvent, out W : BoxWork>(
        override val from: S,
        override val event: E
    ) : BoxOutput<S, E, W>()
}