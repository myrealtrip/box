package com.mrt.box.sample

import androidx.databinding.ViewDataBinding
import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxViewInitializer
import com.mrt.box.be
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityMainBinding

/**
 * Created by jaehochoe on 2020-02-06.
 */
object MainInitView : BoxViewInitializer<MainState, MainEvent> {
    override fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM) {
        b.be<ActivityMainBinding>().vm = vm
    }

    override fun initializeView(v: BoxAndroidView<MainState, MainEvent>, vm: Vm?) {
    }
}