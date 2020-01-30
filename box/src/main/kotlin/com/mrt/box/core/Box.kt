package com.mrt.box.core

import com.mrt.box.android.BoxVm

/**
 * Created by jaehochoe on 2020-01-03.
 */
object Box {

    var isEnableLog: Boolean = false
    var logger: ((String) -> Unit)? = null

    fun disableLog() {
        isEnableLog = false
        this.logger = null
    }

    fun enableLog(logger: ((String) -> Unit)? = null) {
        isEnableLog = true
        this.logger = logger
    }

    fun log(any: Any) {
        if(isEnableLog.not()) return
        logger?.let {
            it(any.toString())
        } ?: println(any)
    }

    fun e(throwable: Throwable) {
        log(throwable.toString())
    }

}

fun <STATE : BoxState, EVENT : BoxEvent, WORK : BoxWork> BoxVm<STATE, EVENT, WORK>.bluePrint(
        initialState: STATE,
        init: BoxBlueprintBuilder<STATE, EVENT, WORK>.() -> Unit
): BoxBlueprint<STATE, EVENT, WORK> {
    return BoxBlueprintBuilder<STATE, EVENT, WORK>(initialState).apply(init).build()
}