package com.mrt.box.android

import androidx.databinding.ViewDataBinding
import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxState
import com.mrt.box.core.Vm

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface BoxViewInitializer<S : BoxState, E : BoxEvent> {
    fun initializeView(v: BoxAndroidView<S, E>, vm: Vm?)
    fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM)
    fun onCleared()
}