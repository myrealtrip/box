package com.mrt.box.sample

import com.mrt.box.android.BoxVm
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.bluePrint
import kotlinx.coroutines.async

/**
 * Created by jaehochoe on 2020-01-02.
 */
class ExampleVm : BoxVm<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val bluePrint: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect>
        get() = bluePrint(ExampleState(0)) {

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
                to(ExampleSideEffect.Finish {
                    it.activity.finish()
                })
            }

            on<ExampleEvent.OnFinishedCleaning> {
                to(copy(count = 0))
            }

            seOnBackground<ExampleSideEffect.AutoCountUp> {
                return@seOnBackground autoCountUpAsync(it.sideEffect.count)
            }
        }

    private fun autoCountUpAsync(count: Int) = async {
        for (i in 0..count) {
            intent(ExampleEvent.OnUpCount)
            kotlinx.coroutines.delay(1000)
        }
        return@async ExampleEvent.OnFinishedCleaning
    }
}