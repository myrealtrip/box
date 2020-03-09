package com.mrt.box.sample

import androidx.lifecycle.ViewModelProviders
import com.mrt.box.android.BoxActivity
import com.mrt.box.android.BoxRenderer
import com.mrt.box.android.BoxViewInitializer

class ExampleActivity
    : BoxActivity<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val renderer: BoxRenderer<ExampleState, ExampleEvent>?
            = ExampleRenderer
    override val viewInitializer: BoxViewInitializer<ExampleState, ExampleEvent>?
            = ExampleInitView
    override val layout: Int
            = R.layout.activity_example

    override val vm: ExampleVm by lazy {
        ViewModelProviders.of(this).get(ExampleVm::class.java)
    }

}
