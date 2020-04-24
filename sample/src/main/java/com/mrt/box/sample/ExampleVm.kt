package com.mrt.box.sample

import android.app.Activity
import com.mrt.box.android.BoxVm
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.bluePrint
import kotlinx.coroutines.async

/**
 * Created by jaehochoe on 2020-01-02.
 */
class ExampleVm : BoxVm<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val bluePrint: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect>
        get() = onCreatedBlueprint()

    fun autoCountUpAsync(count: Int) = async {
        for (i in 0..count) {
            intent(ExampleEvent.OnUpCount)
            kotlinx.coroutines.delay(1000)
        }
        return@async ExampleEvent.OnFinishedCleaning
    }
    
    fun finishActivity(activity: Activity) {
        activity.finish()
    }
}

fun ExampleVm.onCreatedBlueprint() : BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect> {
    return bluePrint(ExampleState(0)) {

        on<ExampleEvent.OnUpCount> {
            to(copy(count = count + 1))
        }
        on<ExampleEvent.OnClick> {
            to(copy(count = count + 1))
        }
        on<ExampleEvent.OnLongClick> {
            to(copy(count = 0))
        }
        on<ExampleEvent.OnClickLayout> {
            to(copy(), ExampleSideEffect.AutoCountUp(3))
        }
        on<ExampleEvent.OnClickFinish> {
            to(ExampleSideEffect.Finish(it.activity))
        }
        
        main<ExampleSideEffect.Finish> {
            finishActivity(it.sideEffect.activity)
        }

        on<ExampleEvent.OnFinishedCleaning> {
            to(copy(count = 0))
        }

        background<ExampleSideEffect.AutoCountUp> {
            return@background autoCountUpAsync(it.sideEffect.count)
        }
    }
}