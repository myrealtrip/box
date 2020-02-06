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
abstract class BoxVm<S : BoxState, E : BoxEvent, W : BoxWork> : ViewModel(),
    CoroutineScope, Vm {

    abstract val bluePrint: BoxBlueprint<S, E, W>

    private var isInitialized = false
    private var stateInternal: S = bluePrint.initialState
    protected val state: S
        get() = stateInternal

    protected var parentState: BoxState? = null

    val stateLiveData = MutableLiveData<S>()

    init {
        stateLiveData.value = state
    }

    private val identifier = Job()
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    override fun intent(event: Any): BoxOutput<S, E, W>? {
        var output: BoxOutput<S, E, W>? = null
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

    private fun model(event: E): BoxOutput<S, E, W> {
        return bluePrint.reduce(stateInternal, event).also { output ->
            Box.log("BoxVm found Event [$event]")
            handleOutput(output)
        }
    }

    fun handleOutput(output: BoxOutput<S, E, W>) {
        when (output) {
            is BoxOutput.Valid -> {
                Box.log("Event to be $output")
                stateInternal = output.to
                view(stateInternal)
                when (output.work) {
                    null, is BoxVoidWork -> return
                    else -> handleWork(output)
                }
            }
            else -> Box.log("Event to be nothing")
        }
    }

    fun handleWork(output: BoxOutput.Valid<S, E, W>) {
        val work = output.work
        Box.log("Output has work $work")
        bluePrint.getWorkOrNull(work)?.let {
            doWork(output, it)
        }
        bluePrint.getHeavyWorkOrNull(work)?.let {
            doWorkInWorkThread(output, it)
        }
    }

    private fun doWork(output: BoxOutput.Valid<S, E, W>, toDo: (BoxOutput.Valid<S, E, W>) -> Any?) {
        Box.log("Do in Foreground: ${output.work}")
        toDo(output).also {
            Box.log("Result is $it for ${output.work}")
            handleResult(it)
        }
    }

    private fun doWorkInWorkThread(
        output: BoxOutput.Valid<S, E, W>,
        toDo: suspend (BoxOutput.Valid<S, E, W>) -> Deferred<Any?>?
    ) {
        Box.log("Do in Background: ${output.work}")
        workThread {
            toDo(output)?.await().also {
                Box.log("Result is $it for ${output.work}")
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
        this.stateLiveData.value = state
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
                stateLiveData.observe(view, Observer {
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

    open fun linkedVms(): Array<BoxVm<BoxState, BoxEvent, BoxWork>>? {
        return null
    }
}