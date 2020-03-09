package com.mrt.box.sample

import android.app.Activity
import com.mrt.box.core.BoxEvent

/**
 * Created by jaehochoe on 2020-01-02.
 */
sealed class ExampleEvent : BoxEvent {
    object OnUpCount : ExampleEvent()
    object OnClick : ExampleEvent()
    object OnLongClick : ExampleEvent()
    object OnFinishedCleaning : ExampleEvent()
    object OnClickLayout : ExampleEvent()
    data class OnClickFinish(val activity: Activity) : ExampleEvent()
}