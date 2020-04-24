package com.mrt.box.sample

import androidx.databinding.ViewDataBinding
import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxViewInitializer
import com.mrt.box.be
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityExampleBinding

/**
 * Created by jaehochoe on 2020-02-06.
 */
object ExampleInitView : BoxViewInitializer<ExampleState, ExampleEvent> {
    override fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM) {
        b.be<ActivityExampleBinding>().vm = vm
    }

    override fun initializeView(v: BoxAndroidView<ExampleState, ExampleEvent>, vm: Vm?) {
    }

    override fun onCleared() {
        
    }
}