package com.mrt.box.sample

import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxRenderer
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityExampleBinding

/**
 * Created by jaehochoe on 2020-01-03.
 */
object ExampleRenderer : BoxRenderer<ExampleState, ExampleEvent> {
    override fun render(v: BoxAndroidView<ExampleState, ExampleEvent>, s: ExampleState, vm: Vm?) {
        val binding = v.binding<ActivityExampleBinding>()
        binding.count = s.count
    }
}