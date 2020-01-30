package com.mrt.box.core

import com.mrt.box.core.internal.BoxKey
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxBlueprint<S : BoxState, E : BoxEvent, W : BoxWork> internal constructor(
        val initialState: S,
        private val outputs: Map<BoxKey<E, E>, (S, E) -> To<S, W>>,
        private val heavyWorks: Map<BoxKey<W, W>, suspend (BoxOutput.Valid<S, E, W>) -> Deferred<Any?>?>,
        private val lightWorks: Map<BoxKey<W, W>, (BoxOutput.Valid<S, E, W>) -> Any?>
) {
    internal fun reduce(state: S, event: E): BoxOutput<S, E, W> {
        return synchronized(this) {
            state.toOutput(event)
        }
    }

    internal fun getHeavyWorkOrNull(work: W): (suspend (BoxOutput.Valid<S, E, W>) -> Deferred<Any?>?)? {
        return synchronized(this) {
            heavyWorks.filter { it.key.check(work) }
                    .map { it.value }
                    .firstOrNull()
        }
    }

    internal fun getWorkOrNull(work: W): ((BoxOutput.Valid<S, E, W>) -> Any?)? {
        return synchronized(this) {
            lightWorks
                    .filter { it.key.check(work) }
                    .map { it.value }
                    .firstOrNull()
        }
    }

    private fun S.toOutput(event: E): BoxOutput<S, E, W> {
        for ((key, stateTo) in outputs) {
            if (key.check(event)) {
                val (toState, work) = stateTo(this, event)
                return BoxOutput.Valid(this, event, toState, work)
            }
        }
        return BoxOutput.Void(this, event)
    }

    data class To<out STATE : BoxState, out WORK : BoxWork> internal constructor(
            val toState: STATE,
            val work: WORK
    )
}