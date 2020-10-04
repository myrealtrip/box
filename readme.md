# Box in Android

At MyRealTrip we are looking into ways to improve the architecture of our app. 

Box is the Android framework from MyRealTrip that we think outside of the box to MVI architecture. Also it was inspired both [Blueprint](#what-is-Blueprint) and coroutines.

This is an introduction regarding the basic concept of Box and how to develop and test apps with it.

Read this in other languages: [English](readme.md), [한국어](readme.ko.md)

## MVI

Box is based on the MVI architecture. For more information on the MVI architecture, please check the following [link](https://medium.com/@jaehochoe/android-mvi-7304bc7e1a84). The data flow in Box is one-way, It is the same as the way introduced in the MVI architecture.

Please refer to the table below.
![box uni-direct cycle](images/box-cycle.png)

## Goals

Box was oriented to address the following goals.

1. Suggestion on the easiest steps to maintain and debug Android app with state using **one immutable state** and **one-way data flow** 
2. Suggestion on How to write patterned testable code based on Blueprint

## Responsibilities

Box consists of State, Event and SideEffect such as MVI architecture. Let's see what each component does.

### State

State represents only one immutable state of the app. Simply put, it contains all the information to render the screen. View draws the screen based on the information in the state.

### Event

State is trigger to change the state of app. It's able to be user interaction and results of SideEffect, described details later. Alternatively, it can be randomly generated in code for screen initialization.

### SideEffect

In addition, SideEffect would perform tasks that cannot be handled by State like Toast, Dialog and Activity transition. It also possible to operate works to do in the background such as API call and I/O by using SideEffect

### What is Blueprint?

As I mentioned above, when you define the relationship between State, Event, and SideEffect generally, you need to implement `Presenter` or `reduce()` function of `ViewModel`.

The `reduce()` function would be implemented to receive both the current State and a new event as arguments. Also it returns a new State and generate SideEffect when necessary. Box uses DSL named Blueprint to define relationship between State, Event and SideEffect instead of implementing `reduce()` function

Blueprint is largely divided into Event definition and SideEffect definition. Please refer to the sample code for detailed usage.

## How do I use Box?

### Dependency

Add the following lines to a project's gradle file.

```gradle
allprojects {
    repositories {
    	// ... Your repositories.
        maven { url "https://jitpack.io" } 
    }
}
```

Add the following lines to an app module's gradle file.

```gradle
dependencies {
	// ... Your dependencies.
	implementation "com.github.myrealtrip:box:$boxVersion"
	implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion"
	implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlinCoroutinesVersion"
}
```

### Quick Start

#### 1. The definition of State, Event and SideEffect

There are own State, Event and SideEffect that each screen uses. Define each component required for the screen when you develop apps with box

   ```kotlin
   data class ExampleState(
       val onProgress: Boolean = false,
       val onError: Throwable? = false,
       val data: Data? = null
   ) : BoxState
   ```
   
   ```kotlin
   sealed class ExampleEvent : BoxEvent {
       object ReqeustData: ExampleEvent()
       data class FetchedData(val data: Data): ExampleEvent()
       data class OnError(val throwable: Throwable): ExampleEvent()
       object OnDataClicked: ExampleEvent()
   }
   ```
   
   ```kotlin
   sealed class ExampleSideEffect : BoxSideEffect {
       object RequestData: ExampleSideEffect()
       object OnDataClicked: ExampleSideEffect()
   }
   ```
   
   
#### 2. The definition of Vm
   
Box provides `BoxVm` was implemented `AndroidViewModel`. BoxVm includes both the Blueprint that defines the relationship between the State, Event, and SideEffects, and the code executed by each SideEffect functions.

For example:

```kotlin
class ExampleVm : BoxVm<ExampleState, ExampleEvent, ExampleSideEffect>() {

   override val Blueprint: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect>
       get() = onCreatedBlueprint()

   fun requestDataAsync() = async {
       return@async Api.requestData().onSuccessed {
           ExampleEvent.FetchedData(it.data)
       }.onFailed {
           ExmpleEvent.OnError(it.error)
       }
   }

   fun moveToNextScreen() {
       startActivity<NextActivity>()
   }
}
```
Oops! These code snippets are not perfect to know regarding Blueprint. 
Box suggest that define the Blueprint generation code as an extension function of the corresponding VM for complete test `BoxVm`
For example:

```kotlin
fun ExampleVm.onCreatedBlueprint() 
            : BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect> {
       return Blueprint(ExampleState()) {
           on<ExampleEvent.ReqeustData> {
               to(copy(onProgress = true), ExampleSideEffect.RequestData)
           }
           background<ExampleSideEffect.RequestData> {
               requestDataAsync()
           }
   
           on<ExampleEvent.FetchedData> {
               to(copy(onProgress = false, data = it.data))
           }
           on<ExampleEvent.OnError> {
               to(copy(onProgress = false, onError = it.error))
           }
   
           on<ExampleEvent.OnDataClicked> {
               to(ExampleSideEffect.OnDataClicked)
           }
           main<ExampleSideEffect.OnDataClicked> {
               moveToNextScreen()
           }
       }
   }
```

When it comes to "Blueprint" is like this:
- When the `ExampleEvent.ReqeustData` occurs, only the  `onProgress` value in the current state is changed to `true` and `ExampleSideEffect.RequestData` is generated. When `ExampleSideEffect.RequestData` occurs, the `requestDataAsync()` function is called from `Diapathcer.Default`.

Let's look at one more case?
- If `ExampleEvent.OnDataClicked` event occurs, it triggers `ExampleSideEffect.OnDataClicked` without changing the current State. Also it call `moveToNextScreen()` in `Dispathcer.Main`

The key of making up a Blueprint is declaring both Event and SideEffect. It uses their own `on()`, `main()`, `background()`, `io()` functions.
It's way too easy, isn’t it? Refer to the images below.

<br/>![box-func-on](images/box-func-on.png)<br/>

- `on()` The function declares the Event to be defined in generic form..
- `on()` The code block of the function receives the current State as `this` and the event to be delivered as `it`.
- `on()` It is the implementation of the `to()` function to define which State this Event will change or which SideEffect should occur.
- `to()` It can have only newly created Events or only SideEffects occuring. Sometimes, you can define both values ​​or not. Of course, if you don't have both values, there is no action

<br/>![box-func-sideeffect](images/box-func-sideeffect.png)<br/>

-  SideEffect can be declared as three types of functions. SideEffect can be declared as generic like the Event Declaration,
-  `main()` function works on `Dispathcer.Main`. It is suitable for exposing dialogs or handling events for screen transition.
-  `background()` function works for common background work
-  `io()` performs background tasks such as I/O tasks, but is suitable for handling low priority tasks.
-  It can refer to the SideEffect, before/after State passed to the `Output.Valid` object delivering to the code block.


#### 3. The definition of View

View implements `BoxActivity` or `BoxFragment`. In this example, we use `BoxActivity`. Please refer the code below.

For example:

```kotlin
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
```

> **What is BoxViewInitializer?**
> 
> It is called once when the View is initialized. It can be used for processing when there is an event that should occur when the view is initialized and when entering the screen, such as setting an adapter in the RecyclerView or ViewPager.

>**What is BoxRenderer?**
> 
> When new State to render has published, the render() function of View will be invoked. We recommend extending BoxRenderer for readability and management of your code.


## Debugging

> Please remember intent() and render()!

Box is designed so that constant state values ​​flow in one direction. 
Every event is updated via the `intent()` function of `BoxVm`. And the new state created through the `intent()` function is passed to the `render()` function of View and drawn. It is relatively easy to debug even when drawing complex screens because points to be checked in error situations that may occur during app development are determined.

## Testability

Box operates based on the Blueprint defined in `BoxVm`. If the Blueprint works as intended, It can assume that the app is working properly. 

If Blueprint would be implemented according to to the guide within a predefined DSL, it can be adopted [Basic Test Class](https://github.com/myrealtrip/box/blob/master/sample/src/test/java/com/mrt/box/sample/VmTest.kt), Box provides.

This basic test class helps mocking and verifying new State and SideEffects created when a specific event is `intent()` in `BoxVm`. 

The test code written by extending this class. Refer the code below :

```kotlin
class ExampleVmTest : VmTest<ExampleState, ExampleEvent, ExampleSideEffect>() {

    override val vm: ExampleVm = mock(ExampleVm::class.java)

    override fun emptyState(): ExampleState {
        return ExampleState()
    }

    @Test
    fun `intent RequestData`() {
        val output = vm.testIntent(ExampleEvent.RequestData)
        assertTrue(output.valid().to.onProgress)
        doHeavySideEffect(output.valid())
        verify(vm).requestDataAsync()
    }

    @Test
    fun `intent FetchedData`() {
        val data = mock(Data::class.java)
        val output = vm.testIntent(ExampleEvent.FetchedData(data))
        assertEquals(output.valid().to.data, data)
    }

    @Test
    fun `intent OnError`() {
        val throwable = mock(Throwable::class.java)
        val output = vm.testIntent(ExampleEvent.OnError(throwable))
        assertEquals(output.valid().to.onError, data)
    }

    @Test
    fun `intent OnDataClicked`() {
        val output = vm.testIntent(ExampleEvent.OnDataClicked)
        doSideEffect(output.valid())
        verify(vm).moveToNextScreen()
    }

    override fun mockBlueprint(): BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect> {
        return vm.onCreatedBlueprint()
    }
}
```


## Compatibility

- Support `Api level 16` or higher
- Developed in Kotlin version `1.3.41`
- Developed in Kotlin Coroutine version `1.3.3`


## Licence

```
The MIT License (MIT)

Copyright (c) 2020 Myrealtrip

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```

