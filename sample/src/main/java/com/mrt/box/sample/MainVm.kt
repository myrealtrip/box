package com.mrt.box.sample

import com.mrt.box.android.BoxVm
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.bluePrint
import kotlinx.coroutines.async

/**
 * Created by jaehochoe on 2020-01-02.
 */
class MainVm : BoxVm<MainState, MainEvent, MainSideEffect>() {

    override val bluePrint: BoxBlueprint<MainState, MainEvent, MainSideEffect>
        get() = bluePrint(MainState(0)) {

            on<MainEvent.OnUpCount> {
                to(copy(count = count + 1))
            }
            on<MainEvent.OnClick> {
                to(copy(count = count + 1))
            }
            on<MainEvent.OnLongClick> {
                to(copy(count = 0))
            }
            on<MainEvent.OnClickLayout> {
                to(copy(), MainSideEffect.AutoCountUp(3))
            }
            on<MainEvent.OnClickFinish> {
                to(MainSideEffect.Finish {
                    it.activity.finish()
                })
            }

            on<MainEvent.OnFinishedCleaning> {
                to(copy(count = 0))
            }

            seOnBackground<MainSideEffect.AutoCountUp> {
                return@seOnBackground autoCountUpAsync(it.sideEffect.count)
            }
        }

    private suspend fun autoCountUpAsync(count: Int) = async {
        for (i in 0..count) {
            intent(MainEvent.OnUpCount)
            kotlinx.coroutines.delay(1000)
        }
        return@async MainEvent.OnFinishedCleaning
    }
}