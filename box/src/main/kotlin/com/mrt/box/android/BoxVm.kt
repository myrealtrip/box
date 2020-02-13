package com.mrt.box.android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.mrt.box.android.event.InAppEvent
import com.mrt.box.core.*
import com.mrt.box.isValidEvent
import kotlinx.coroutines.*
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
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    override fun intent(event: Any): BoxOutput<S, E, SE>? {
        var output: BoxOutput<S, E, SE>? = null
        if (isValidEvent(event)) {
            output = model(event as E)
        } else {
            Box.log("Intent was not BoxEvent")
        }

        linkedVms()?.let {
            for (vm in it) {
                vm.parentState = state
                if (vm.isValidEvent(event)) {
                    vm.intent(event)
                }
            }
        }
        return output
    }

    private fun model(event: E): BoxOutput<S, E, SE> {
        return bluePrint.reduce(stateInternal, event).also { output ->
            Box.log("BoxVm found Event [$event]")
            handleOutput(output)
        }
    }

    fun handleOutput(output: BoxOutput<S, E, SE>) {
        when (output) {
            is BoxOutput.Valid -> {
                Box.log("Event to be $output")
                stateInternal = output.to
                view(stateInternal)
                when (output.sideEffect) {
                    null, is BoxVoidSideEffect -> return
                    else -> handleSideEffect(output)
                }
            }
            else -> Box.log("Event to be nothing")
        }
    }

    fun handleSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        val sideEffect = output.sideEffect
        Box.log("Output has sideEffect $sideEffect")
        bluePrint.workOrNull(sideEffect)?.let {
            doWork(output, it)
        }
        bluePrint.heavyWorkOrNull(sideEffect)?.let {
            doWorkInWorkThread(output, it)
        }
    }

    private fun doWork(
        output: BoxOutput.Valid<S, E, SE>,
        toDo: (BoxOutput.Valid<S, E, SE>) -> Any?
    ) {
        Box.log("Do in Foreground: ${output.sideEffect}")
        toDo(output).also {
            Box.log("Result is $it for ${output.sideEffect}")
            handleResult(it)
        }
    }

    private fun doWorkInWorkThread(
        output: BoxOutput.Valid<S, E, SE>,
        toDo: suspend (BoxOutput.Valid<S, E, SE>) -> Deferred<Any?>?
    ) {
        Box.log("Do in Background: ${output.sideEffect}")
        workThread {
            toDo(output)?.await().also {
                Box.log("Result is $it for ${output.sideEffect}")
                handleResult(it)
            }
        }
    }

    fun handleResult(result: Any?) {
        when (result) {
            is BoxState -> {
                this.stateInternal = result as S
                mainThread { view(this.stateInternal) }
            }
            is BoxEvent -> {
                mainThread { intent(result as E) }
            }
        }
    }

    private fun view(state: S) {
        this.currentState.value = state
    }

    protected fun mainThread(block: suspend () -> Unit) {
        val job = launch {
            block()
        }
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        jobs.add(job)
    }

    protected fun workThread(block: suspend () -> Unit) {
        val job = launch(Dispatchers.Main) {
            block()
        }
        job.invokeOnCompletion {
            jobs.remove(job)
        }
        jobs.add(job)
    }

    fun <V : BoxAndroidView<S, E>> bind(view: V) {
        when (view) {
            is LifecycleOwner -> {
                currentState.observe(view, Observer {
                    Box.log("View will view by $it")
                    view.render(it as S)
                })
                if (isInitialized.not()) {
                    Box.log("Vm has initial state as ${bluePrint.initialState}")
                    view(bluePrint.initialState)
                    isInitialized = true
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        identifier.cancel()
        jobs.forEach { job ->
            job.cancel()
        }
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
}