package com.mrt.box.android

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxState
import com.mrt.box.core.BoxView
import com.mrt.box.be


/**
 * Created by jaehochoe on 2020-01-01.
 */
interface BoxAndroidView<S : BoxState, E : BoxEvent> : BoxView<S, E> {
    val binding: ViewDataBinding?
    val layout: Int

    open fun activity(): AppCompatActivity

    open fun fragment(): Fragment

    fun <B : ViewDataBinding> binding(): B {
        return binding.be()
    }
}