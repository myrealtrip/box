package com.mrt.box.core

import com.mrt.box.core.internal.BoxKey
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxBlueprintBuilder<STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork>(val initialState: STATE) {
    val outputs = mutableMapOf<BoxKey<EVENT, EVENT>, (STATE, EVENT) -> BoxBlueprint.To<STATE, WORK>>()
    val heavyWorks = mutableMapOf<BoxKey<WORK, WORK>, suspend (BoxOutput.Valid<STATE, EVENT, WORK>) -> Deferred<Any?>?>()
    val lightWorks = mutableMapOf<BoxKey<WORK, WORK>, (BoxOutput.Valid<STATE, EVENT, WORK>) -> Any?>()

    inline fun <reified W : WORK> workInBackground(
            noinline init: suspend (BoxOutput.Valid<STATE, EVENT, W>) -> Deferred<Any?>?
    ) {
        heavyWorks[BoxKey<WORK, W>(W::class.java)] = init as (suspend (BoxOutput.Valid<STATE, EVENT, WORK>) -> Deferred<Any?>)
    }

    inline fun <reified W : WORK> work(
            noinline init: (BoxOutput.Valid<STATE, EVENT, W>) -> Any?
    ) {
        lightWorks[BoxKey<WORK, W>(W::class.java)] = init as (BoxOutput.Valid<STATE, EVENT, WORK>) -> Any?
    }

    inline fun <reified E : EVENT> on(
            noinline to: STATE.(E) -> BoxBlueprint.To<STATE, WORK>
    ) {
        outputs[BoxKey<EVENT, E>(E::class.java)] = { state, event ->
            to(state, event as E)
        }
    }

    @Suppress("UNUSED") // The unused warning is probably a compiler bug.
    fun STATE.to(state: STATE, work: WORK? = null) = BoxBlueprint.To(state, work
            ?: BoxVoidWork as WORK)

    @Suppress("UNUSED") // The unused warning is probably a compiler bug.
    fun STATE.to(work: WORK? = null) = this@to.to(this, work)

    fun build(): BoxBlueprint<STATE, EVENT, WORK> {
        return BoxBlueprint(
                initialState,
                outputs.toMap(),
                heavyWorks.toMap(),
                lightWorks.toMap()
        )
    }

}