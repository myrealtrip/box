package com.mrt.box.sample

import android.app.Activity
import com.mrt.box.core.BoxBlueprint
import junit.framework.Assert.assertTrue
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Created by jaehochoe on 2020-04-24.
 */
class ExampleVmTest : VmTest<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val vm: ExampleVm = mock(ExampleVm::class.java)

    override fun emptyState(): ExampleState {
        return ExampleState()
    }

    @Test
    fun `intent OnUpCount`() {
        val output = vm.testIntent(ExampleEvent.OnUpCount)
        assertTrue(output.valid().to.count == 1)
    }

    @Test
    fun `intent OnClick`() {
        val output = vm.testIntent(ExampleEvent.OnClick)
        assertTrue(output.valid().to.count == 1)
    }

    @Test
    fun `intent OnLongClick`() {
        val output = vm.testIntent(ExampleEvent.OnLongClick)
        assertTrue(output.valid().to.count == 0)
    }

    @Test
    fun `intent OnClickLayout`() {
        val output = vm.testIntent(ExampleEvent.OnClickLayout)
        runBlocking {
            doHeavySideEffect(output.valid())
            verify(vm).autoCountUpAsync(3)
        }
    }
    @Test
    fun `intent OnClickFinish`() {
        val activity = mock(Activity::class.java)
        val output = vm.testIntent(ExampleEvent.OnClickFinish(activity))
        doSideEffect(output.valid())
        verify(vm).finishActivity(activity)
    }

    override fun mockBlueprint(): BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect> {
        return vm.onCreatedBlueprint()
    }
}