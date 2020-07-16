package com.mrt.box.sample

import androidx.lifecycle.ViewModelProviders
import com.mrt.box.android.BoxActivity
import com.mrt.box.android.BoxRenderer
import com.mrt.box.android.BoxRenderingScope
import com.mrt.box.android.BoxViewInitializer

class ExampleActivity
    : BoxActivity<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val renderer: BoxRenderer? = ExampleRenderer
    override val layout: Int = R.layout.activity_example
    override val viewInitializer: BoxViewInitializer? = null
    override val vm: ExampleVm by lazy {
        ViewModelProviders.of(this).get(ExampleVm::class.java)
    }

    override val partialRenderers: Map<BoxRenderingScope, BoxRenderer>? = mapOf(
        ProgressScope to ProgressRenderer,
        ColorScope to ColorRenderer
    )
}
