package com.mrt.box.sample

import android.graphics.Color
import com.mrt.box.android.BoxAndroidView
import com.mrt.box.android.BoxStateRenderer
import com.mrt.box.core.Vm
import com.mrt.box.sample.databinding.ActivityExampleBinding

/**
 * Created by jaehochoe on 14/07/2020.
 */
object ColorRenderer : BoxStateRenderer<ExampleState> {
    override fun render(v: BoxAndroidView, s: ExampleState, vm: Vm?) {
        println("ColorRenderer.render() $s")
        val binding = v.binding<ActivityExampleBinding>()
        when(s.progress >= 100) {
            true -> binding.root.setBackgroundColor(Color.WHITE)
            else -> binding.root.setBackgroundColor(Color.argb(255 * s.progress / 100, 0, 0, 0))
        }
    }
}