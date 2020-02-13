package com.mrt.box.core

import com.mrt.box.core.internal.BoxKey
import kotlinx.coroutines.Deferred

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxBlueprint<S : BoxState, E : BoxEvent, SE : BoxSideEffect> internal constructor(
        val initialState: S,
        private val outputs: Map<BoxKey<E, E>, (S, E) -> To<S, SE>>,
        private val heavyWorks: Map<BoxKey<SE, SE>, suspend (BoxOutput.Valid<S, E, SE>) -> Deferred<Any?>?>,
        private val lightWorks: Map<BoxKey<SE, SE>, (BoxOutput.Valid<S, E, SE>) -> Any?>
) {
    fun reduce(state: S, event: E): BoxOutput<S, E, SE> {
        return synchronized(this) {
            state.toOutput(event)
        }
    }

    internal fun getHeavyWorkOrNull(sideEffect: SE): (suspend (BoxOutput.Valid<S, E, SE>) -> Deferred<Any?>?)? {
        return synchronized(this) {
            heavyWorks.filter { it.key.check(sideEffect) }
                    .map { it.value }
                    .firstOrNull()
        }
    }

    internal fun getWorkOrNull(sideEffect: SE): ((BoxOutput.Valid<S, E, SE>) -> Any?)? {
        return synchronized(this) {
            lightWorks
                    .filter { it.key.check(sideEffect) }
                    .map { it.value }
                    .firstOrNull()
        }
    }

    private fun S.toOutput(event: E): BoxOutput<S, E, SE> {
        for ((key, stateTo) in outputs) {
            if (key.check(event)) {
                val (toState, sideEffect) = stateTo(this, event)
                return BoxOutput.Valid(this, event, toState, sideEffect)
            }
        }
        return BoxOutput.Void(this, event)
    }

    data class To<out STATE : BoxState, out SIDE_EFFECT : BoxSideEffect> internal constructor(
            val toState: STATE,
            val sideEffect: SIDE_EFFECT
    )
}