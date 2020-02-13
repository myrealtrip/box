package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-01.
 */
sealed class BoxOutput<out S : BoxState, out E : BoxEvent, out SE : BoxSideEffect> {
    abstract val from: S
    abstract val event: E

    data class Valid<out S : BoxState, out E : BoxEvent, out SE : BoxSideEffect>(
        override val from: S,
        override val event: E,
        val to: S,
        val sideEffect: SE
    ) : BoxOutput<S, E, SE>()

    data class Void<out S : BoxState, out E : BoxEvent, out SE : BoxSideEffect>(
        override val from: S,
        override val event: E
    ) : BoxOutput<S, E, SE>()
}