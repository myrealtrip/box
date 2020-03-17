package com.mrt.box.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.android.event.event.BoxInAppEvent
import com.mrt.box.core.Box
import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxSideEffect
import com.mrt.box.core.BoxState
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch


/**
 * Created by jaehochoe on 2020-01-03.
 */
abstract class BoxFragment<S : BoxState, E : BoxEvent, SE : BoxSideEffect> : Fragment(),
    BoxAndroidView<S, E> {

    abstract val isNeedLazyLoading: Boolean
    private var isBound = false

    private val rendererList: List<BoxRenderer<S, E>> by lazy {
        val list = (extraRenderer() ?: mutableListOf())
        renderer?.let {
            list.add(0, it)
        }
        list
    }

    abstract val renderer: BoxRenderer<S, E>?

    abstract val viewInitializer: BoxViewInitializer<S, E>?

    abstract val vm: BoxVm<S, E, SE>?

    private lateinit var bindingTemp: ViewDataBinding

    override val binding: ViewDataBinding? by lazyOf(bindingTemp)

    open fun preOnCreateView(savedInstanceState: Bundle?) {
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        preOnCreateView(savedInstanceState)
        return if (layout > 0) {
            bindingTemp = DataBindingUtil.inflate(inflater, layout, container, false)
            binding?.lifecycleOwner = this
            vm?.let {
                if (isNeedLazyLoading.not()) {
                    bindingVm()
                }
                viewInitializer?.bindingVm(binding, it)
                it.launch {
                    subscribe(BoxInAppEvent.asChannel())
                }
            }
            viewInitializer?.initializeView(this, vm)
            binding?.root
        } else
            super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        viewInitializer?.onCleared()
        super.onDestroy()
    }

    private suspend fun subscribe(channel: ReceiveChannel<InAppEvent>) {
        var isNeedSkipFirstEvent = channel.isEmpty.not()
        for (inAppEvent in channel) {
            if (isNeedSkipFirstEvent.not()) {
                Box.log { "InAppEvent = $inAppEvent in ${this@BoxFragment}" }
                onSubscribe(inAppEvent)
            } else
                isNeedSkipFirstEvent = false
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible && isNeedLazyLoading && !isBound) {
            bindingVm()
        }
    }

    private fun bindingVm() {
        vm?.let {
            it?.bind(this@BoxFragment)
            viewInitializer?.bindingVm(binding, it)
            isBound = true
        }
    }


    override fun render(state: S) {
        rendererList.forEach { renderer ->
            renderer.render(this, state, vm)
        }
    }

    override fun intent(event: E) {
        vm?.intent(event)
    }

    @Suppress("UNUSED")
    fun extraRenderer(): MutableList<BoxRenderer<S, E>>? {
        return null
    }

    override fun activity(): AppCompatActivity {
        if (activity !is AppCompatActivity)
            error("AppCompatActivity is required")

        return activity as AppCompatActivity
    }

    private fun onSubscribe(inAppEvent: InAppEvent) {
        vm?.onSubscribe(inAppEvent)
    }
}