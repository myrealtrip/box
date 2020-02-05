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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


/**
 * Created by jaehochoe on 2020-01-01.
 */
abstract class BoxVm<S : BoxState, E : BoxEvent, SE : BoxWork> : ViewModel(),
    CoroutineScope, Vm {

    abstract val bluePrint: BoxBlueprint<S, E, SE>

    private var isInitialized = false
    private var stateInternal: S = bluePrint.initialState
    private val state: S
        get() = stateInternal

    protected var parentState: BoxState? = null
    val stateLiveData = MutableLiveData<BoxState>()

    private val identifier = Job()
    private val jobs = mutableListOf<Job>()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + identifier

    private fun model(event: E) {
        val output: BoxOutput<S, E, SE> = bluePrint.reduce(stateInternal, event)
        Box.log("Intent was $output")
        if (output is BoxOutput.Valid) {
            stateInternal = output.to
            view(stateInternal)
            output.work?.let { sideEffect ->
                Box.log("Transition has side effect $sideEffect")
                var result: Any? = null
                var isHeavyWork = false
                var toDo: Any? = bluePrint.getWorkOrNull(sideEffect)
                if (toDo == null) {
                    toDo = bluePrint.getHeavyWorkOrNull(sideEffect)

                    if (toDo == null)
                        return@model
                    else
                        isHeavyWork = true
                }
                when (isHeavyWork) {
                    true -> {
                        val toDo = bluePrint.getHeavyWorkOrNull(sideEffect) ?: return@model
                        Box.log("Do in Background: $sideEffect")
                        workThread {
                            result = toDo(output)?.await()
                            Box.log("Result is $result for $sideEffect")
                            handleResult(result)
                        }
                    }
                    else -> {
                        val toDo = bluePrint.getWorkOrNull((sideEffect)) ?: return@model
                        Box.log("Do in Foreground: $sideEffect")
                        result = toDo(output)
                        Box.log("Result is $result for $sideEffect")
                        handleResult(result)
                    }
                }
            }
        }
    }

    private fun handleResult(result: Any?) {
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

    override fun intent(event: Any) {
        if (isValidEvent(event)) {
            model(event as E)
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