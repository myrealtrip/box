package com.mrt.box.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.core.Box
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxOutput
import com.mrt.box.core.BoxSideEffect
import com.mrt.box.core.BoxState
import com.mrt.box.core.BoxVoidSideEffect
import com.mrt.box.core.HeavyWork
import com.mrt.box.core.IOWork
import com.mrt.box.core.LightWork
import com.mrt.box.core.Vm
import com.mrt.box.isValidEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Created by jaehochoe on 2020-01-01.
 */
abstract class BoxVm<S : BoxState, E : BoxEvent, SE : BoxSideEffect> : ViewModel(), CoroutineScope,
    Vm {

    abstract val bluePrint: BoxBlueprint<S, E, SE>

    private var isInitialized = false
    private var stateInternal: S = bluePrint.initialState
    protected val state: S
        get() = stateInternal

    protected var parentState: BoxState? = null

    val currentState = MutableLiveData<S>()

    init {
        currentState.value = state
    }

    private val identifier = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    override fun intent(event: Any): BoxOutput<S, E, SE>? {
        var output: BoxOutput<S, E, SE>? = null
        if (isValidEvent(event)) {
            output = model(event as E)
        } else {
            Box.log { "Intent was not BoxEvent" }
        }

        linkedVms()?.forEach { vm ->
            vm.parentState = state
            if (vm.isValidEvent(event)) {
                vm.intent(event)
            }
        }
        return output
    }

    private fun model(event: E): BoxOutput<S, E, SE> {
        return bluePrint.reduce(stateInternal, event).also { output ->
            Box.log { "BoxVm found Event [$event]" }
            handleOutput(output)
        }
    }

    @SuppressWarnings("unchecked")
    fun handleOutput(output: BoxOutput<S, E, SE>) {
        when (output) {
            is BoxOutput.Valid -> {
                Box.log { "Event to be $output" }
                stateInternal = output.to
                view(output.to)
                stateInternal = output.to.consumer() as S
                when (output.sideEffect) {
                    null, is BoxVoidSideEffect -> return
                    else -> handleSideEffect(output)
                }
            }
            else -> Box.log { "Event to be nothing" }
        }
    }

    @SuppressWarnings("unchecked")
    fun handleSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        val sideEffect = output.sideEffect
        Box.log { "Output has sideEffect $sideEffect" }
        bluePrint.workOrNull(sideEffect)?.let {
            doWork(output, it)
        }
        bluePrint.heavyWorkOrNull(sideEffect)?.let {
            doWorkInBackgroundThread(output, it)
        }
        bluePrint.ioWorkOrNull(sideEffect)?.let {
            doWorkInIOThread(output, it)
        }
    }

    private fun doWork(
        output: BoxOutput.Valid<S, E, SE>,
        toDo: LightWork<S, E, SE>
    ) {
        Box.log { "Do in Foreground: ${output.sideEffect}" }
        toDo(output).also {
            Box.log { "Result is $it for ${output.sideEffect}" }
            handleResult(it)
        }
    }

    private fun doWorkInBackgroundThread(
        output: BoxOutput.Valid<S, E, SE>,
        toDo: HeavyWork<S, E, SE>
    ) {
        Box.log { "Do in Background: ${output.sideEffect}" }
        workThread {
            toDo(output)?.await().also {
                Box.log { "Result is $it for ${output.sideEffect}" }
                handleResult(it)
            }
        }
    }

    private fun doWorkInIOThread(
        output: BoxOutput.Valid<S, E, SE>,
        toDo: IOWork<S, E, SE>
    ) {
        Box.log { "Do in IO: ${output.sideEffect}" }
        ioThread {
            toDo(output)?.await().also {
                Box.log { "Result is $it for ${output.sideEffect}" }
                handleResult(it)
            }
        }
    }

    @SuppressWarnings("unchecked")
    fun handleResult(result: Any?) {
        when (result) {
            is BoxState -> {
                this.stateInternal = result as S
                mainThread { view(result) }
                this.stateInternal = result.consumer() as S
            }
            is BoxEvent -> {
                mainThread { intent(result as E) }
            }
        }
    }

    private fun view(state: S) {
        this.currentState.value = state
    }

    @SuppressWarnings("unchecked")
    protected fun mainThread(block: () -> Unit) {
        launch {
            block()
        }
    }

    @SuppressWarnings("unchecked")
    protected fun workThread(block: suspend () -> Unit) {
        launch(Dispatchers.Default) {
            block()
        }
    }

    @SuppressWarnings("unchecked")
    protected fun ioThread(block: suspend () -> Unit) {
        launch(Dispatchers.IO) {
            block()
        }
    }

    fun <V : BoxAndroidView<S, E>> bind(view: V) {
        when (view) {
            is LifecycleOwner -> {
                currentState.observe(view, Observer {
                    Box.log { "View will view by $it" }
                    view.render(it)
                })
                if (isInitialized.not() && isSkipInitialState().not()) {
                    Box.log { "Vm has initial state as ${bluePrint.initialState}" }
                    view(bluePrint.initialState)
                    isInitialized = true
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        identifier.cancel()
    }

    open fun onActivityResult(
        activity: AppCompatActivity,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        linkedVms()?.forEach {
            it.onActivityResult(activity, requestCode, resultCode, data)
        }
    }

    open fun onNewIntent(intent: Intent?) {
        linkedVms()?.forEach {
            it.onNewIntent(intent)
        }
    }

    open fun onSubscribe(inAppEvent: InAppEvent) {
    }

    open fun linkedVms(): Array<BoxVm<BoxState, BoxEvent, BoxSideEffect>>? {
        return null
    }

    open fun isSkipInitialState(): Boolean {
        return false
    }
}