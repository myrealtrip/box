package com.mrt.box.core

import com.mrt.box.core.internal.BoxKey
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxBlueprintBuilder<STATE : BoxState, EVENT : BoxEvent, SIDE_EFFECT : BoxSideEffect>(private val initialState: STATE) {
    val outputs = mutableMapOf<BoxKey<EVENT, EVENT>, (STATE, EVENT) -> BoxBlueprint.To<STATE, SIDE_EFFECT>>()
    val heavyWorks = mutableMapOf<BoxKey<SIDE_EFFECT, SIDE_EFFECT>, suspend (BoxOutput.Valid<STATE, EVENT, SIDE_EFFECT>) -> Deferred<Any?>?>()
    val lightWorks = mutableMapOf<BoxKey<SIDE_EFFECT, SIDE_EFFECT>, (BoxOutput.Valid<STATE, EVENT, SIDE_EFFECT>) -> Any?>()

    inline fun <reified W : SIDE_EFFECT> seInBackground(
            noinline init: suspend (BoxOutput.Valid<STATE, EVENT, W>) -> Deferred<Any?>?
    ) {
        heavyWorks[BoxKey<SIDE_EFFECT, W>(W::class.java)] = init as (suspend (BoxOutput.Valid<STATE, EVENT, SIDE_EFFECT>) -> Deferred<Any?>)
    }

    inline fun <reified W : SIDE_EFFECT> se(
            noinline init: (BoxOutput.Valid<STATE, EVENT, W>) -> Any?
    ) {
        lightWorks[BoxKey<SIDE_EFFECT, W>(W::class.java)] = init as (BoxOutput.Valid<STATE, EVENT, SIDE_EFFECT>) -> Any?
    }

    inline fun <reified E : EVENT> on(
            noinline to: STATE.(E) -> BoxBlueprint.To<STATE, SIDE_EFFECT>
    ) {
        outputs[BoxKey<EVENT, E>(E::class.java)] = { state, event ->
            to(state, event as E)
        }
    }

    @Suppress("UNUSED") // The unused warning is probably a compiler bug.
    fun STATE.to(state: STATE, sideEffect: SIDE_EFFECT? = null) = BoxBlueprint.To(state, sideEffect
            ?: BoxVoidSideEffect as SIDE_EFFECT)

    @Suppress("UNUSED") // The unused warning is probably a compiler bug.
    fun STATE.to(sideEffect: SIDE_EFFECT? = null) = this@to.to(this, sideEffect)

    fun build(): BoxBlueprint<STATE, EVENT, SIDE_EFFECT> {
        return BoxBlueprint(
                initialState,
                outputs.toMap(),
                heavyWorks.toMap(),
                lightWorks.toMap()
        )
    }

}