package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-01.
 */
sealed class BoxOutput<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork> {
    abstract val from: STATE
    abstract val event: EVENT

    data class Valid<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork>(
            override val from: STATE,
            override val event: EVENT,
            val to: STATE,
            val work: WORK
    ) : BoxOutput<STATE, EVENT, WORK>()

    data class Void<out STATE : BoxState, out EVENT : BoxEvent, out WORK : BoxWork>(
            override val from: STATE,
            override val event: EVENT
    ) : BoxOutput<STATE, EVENT, WORK>()
}