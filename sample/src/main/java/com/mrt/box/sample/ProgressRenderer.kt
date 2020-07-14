package com.mrt.box.sample

import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxStateRenderer
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityExampleBinding

/**
 * Created by jaehochoe on 14/07/2020.
 */
object ProgressRenderer : BoxStateRenderer<ExampleState> {
    override fun render(v: BoxAndroidView, s: ExampleState, vm: Vm?) {
        println("ProgressRenderer.render() $s")
        val binding = v.binding<ActivityExampleBinding>()
        binding.progress.progress = s.progress
    }
}