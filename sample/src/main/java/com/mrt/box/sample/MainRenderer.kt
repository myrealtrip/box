package com.mrt.box.sample

import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxRenderer
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityMainBinding

/**
 * Created by jaehochoe on 2020-01-03.
 */
object MainRenderer : BoxRenderer<MainState, MainEvent> {
    override fun render(v: BoxAndroidView<MainState, MainEvent>, s: MainState, vm: Vm?) {
        val binding = v.binding<ActivityMainBinding>()
        binding.count = s.count
    }
}