package com.mrt.box.sample

import android.app.Activity
import com.mrt.box.core.BoxEvent

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class MainEvent : BoxEvent {
    object OnUpCount : MainEvent()
    object OnClick : MainEvent()
    object OnLongClick : MainEvent()
    object OnFinishedCleaning : MainEvent()
    object OnClickLayout : MainEvent()
    data class OnClickFinish(val activity: Activity) : MainEvent()
}