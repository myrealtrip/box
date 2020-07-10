package com.mrt.box.sample

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mrt.box.android.BoxVm
import com.mrt.box.core.BoxBlueprint
import com.mrt.box.core.BoxEvent
import com.mrt.box.core.BoxOutput
import com.mrt.box.core.BoxSideEffect
import com.mrt.box.core.BoxState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.mockito.Mockito

/**
 * Created by jaehochoe on 2020-04-24.
 */
abstract class VmTest<S : BoxState, E : BoxEvent, SE : BoxSideEffect> {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val mainThreadSurrogate = newSingleThreadContext("UI thread")

    abstract val vm: BoxVm<S, E, SE>

    abstract fun emptyState(): S

    @Before
    fun setUp() {
        Dispatchers.setMain(mainThreadSurrogate)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        mainThreadSurrogate.close()
    }

    protected fun BoxVm<S, E, SE>.testIntent(
        event: E,
        state: S = emptyState()
    ): BoxOutput<S, E, SE> {
        val output = mockBlueprint().reduce(state, event)
        Mockito.`when`(intent(event)).thenReturn(output)
        return intent(event) as BoxOutput.Valid<S, E, SE>
    }

    protected fun <S : BoxState, E : BoxEvent, SE : BoxSideEffect> BoxOutput<S, E, SE>.valid(): BoxOutput.Valid<S, E, SE> {
        return this as BoxOutput.Valid<S, E, SE>
    }


    protected suspend fun doIoSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().ioWorkOrNull(output.sideEffect)!!(output.valid())
    }

    protected suspend fun doHeavySideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().heavyWorkOrNull(output.sideEffect)!!(output.valid())
    }

    protected fun doSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().workOrNull(output.sideEffect)!!(output.valid())
    }

    abstract fun mockBlueprint(): BoxBlueprint<S, E, SE>
}

