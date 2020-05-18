# Box in Android

Myrealtrip 모바일 팀은 보다 빠르고 안정적으로 Android 앱을 개발 할 수 있도록 우리 앱 아키텍쳐를 향상시키는 방법을 항상 고민하고 있습니다. Box는 StateMachine 에서 영감을 얻은 `Blueprint` 개념과 Kotlin의 코루틴을 활용한 MVI 기반의 Android 앱 아키텍처 프레임 워크입니다. 이 페이지에서는 Box의 기본 컨셉과 Box를 사용하여 어떻게 테스트 가능한 구조의 앱을 개발할 수 있을지에 대해 소개 합니다.



## MVI

Box는 MVI 아키텍처를 기반으로 하고 있습니다. MVI 아키텍처에 대한 자세한 내용은 다음 [링크](https://link.medium.com/0cBFY3nEC4)를 확인해주세요. MVI 아키텍처에서 소개하는 데이터 흐름과 동일하게 Box의 데이터 흐름은 단방향입니다. 아래 표를 참고해주세요.
BoxView.intent(event) ->  BoxVm.reduce(state, event) -> BoxView.render(newState)



## Goals

Box는 다음과 같은 목표를 해결하기 위해 디자인되었습니다.

1. 앱의 현재 상태를 표현하는 하나의 불변 상태와 단방향 데이터 흐름으로 제어와 디버깅이 용이한 Android 앱 개발 방법 제안
2. 규격화된 Blueprint를 기반으로 패턴화된 테스트 코드 작성법 제안



## Basic Concept of Component

Box는 `View`와 `Vm`이라는 두 가지 구성요소로 이뤄져있습니다. 이 중 `View` 는 앱의 상태 (`State`) 를 전달 받아 화면에 랜더링 하고 사용자의 반응을 입력 받아 이벤트 (`Event`) 형태로 `Vm`에 전달하는 역할을 맡습니다. `Vm`은 전달받은 이벤트를  `Blueprint` 에 미리 정의한 내용과 현재 상태를 참조하여 새로운 상태 (`State`) 로 만들고 필요에 따라 사이드이펙트 (`SideEffect`)를 발생시킵니다. 각 컴포넌트들에 대한 설명은 아래에 이어집니다.



#### BoxView - BoxActivity, BoxFragment

BoxView는 `BoxState`를 입력값으로 화면을 그립니다. 또 그려진 화면에서 일어난 사용자 반응과 특정한 시점에 수행되어야할 동작들을 `BoxEvent` 형태로 발생시킵니다. Box는 안드로이드 개발을 위하여 `BoxVm`와 연동하여 동작하는 `BoxActivity` 와 `BoxFragment` 를 View로 제공합니다.  Box를 사용하기 위해서는 `BoxActivity` 를 사용하여  Activity를 구현해야합니다. 일반적인 `BoxActivity` 의 모습은 아래 코드를 참고해주세요.

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

- 제네릭 선언: 해당 `BoxView`에서 사용할 `BoxState`, `BoxEvent`, `BoxSideEffect` 타입을 제네릭으로 선언해야 합니다.  
- `BoxRenderer`: `Vm`로 부터 전달 받은 `BoxState`를 이용하여 화면을 그릴 Renderer 입니다. 화면이 간단하다면 선언하지 않고 BoxView의 `render()` 함수를 override 해도 무방합니다.
- `BoxViewInitializer`: `BoxView`(`BoxActivity`/`BoxFragment`) 가 생성될때 최초 1회 호출됩니다. 뷰 초기화를 수행합니다. 
- layout: 해당 화면을 구성할 layout 리소스를 지정합니다. 화면이 없는 `BoxView` 일 경우 생략 가능합니다. 단, layout은 데이터 바인딩을 지원해야합니다.
- `vm`: `BoxView`에서 처리할 `BoxState`, `BoxEvent`, `BoxSideEffect` 를 `BoxBlueprint` 형태로 정의합니다. 정의된 내용은  `Vm`이 `intent`를 통해 `BoxEvent`를 전달 받으면 자동으로 처리됩니다.



#### BoxVm

Box와 전통적인 MVI 아키텍처는 크고 작은 차이점이 있지만 그 중 하나는 `reduce()` 함수를 구현할 필요가 없다는 점입니다. 대신 Event와 State, SideEffect의 관계를 정의한`Blueprint` 를 작성하여 BoxView에서 사용될 상태(BoxState), 이벤트(BoxEvent), 부가 효과(BoxSideEffect) 를 모델링 할 수 있습니다. `BoxVm` 은 아래와 같은 특징을 갖습니다.

- `Vm`의 구현체로 `BoxState`와 `BoxEvent`, `BoxSideEffect`를 처리하기 위한 `BoxBlueprint` 정보가 정의 되어 있습니다. 
- `BoxView`에서 전달 받은 `BoxEvent` 와 최근 `BoxState` 를 이용하여 새로운 `BoxState`와 `BoxSideEffect`를 생성합니다. 생성된 새로운 `BoxState`는 `BoxView`에 바로 전달되어 랜더링 되고 동시에 필요할 경우 `BoxSideEffect`가 수행됩니다.

```kotlin
class ExampleVm : BoxVm<ExampleState, ExampleEvent, ExampleSideEffect>() {
    override val bluePrint: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect>        
}
```

1. `BoxState`: `BoxView`에 그려지는 앱의 상태를 나타냅니다. 매번 MVI 사이클을 수행하면서 단 하나의 불면 객체로 생성됩니다.
2. `BoxEvent`: 사용자의 입력과 같이 앱의 상태를 변경해야 할때 생성되어 `Vm`으로 전달하는 모델 객체입니다.  
3. `BoxSideEffect`: `Vm`의 `BoxBlueprint`에 정의되어 있는 규칙에 따라 필요할 경우 수행되어야 할 SideEffect입니다. SideEffect는 MVI 일반적 사이클에서 벗어나는 액션들 즉, 액티비티 이동, 서버 통신, 다이얼로그 노출, 복잡한 백그라운드 작업 등을 수행합니다. SideEffect의 수행 결과가 `BoxEvent`일 경우에는 `Vm`의 `intent`함수로 전달되어 다시 새로운 사이클을 시작합니다.



## Quick Start

간단한 앱을 작성하며 Box로 안드로이드 앱을 개발하는 방법을 함께 확인해봅시다. 

#### State, Event, SideEffect 정의

1. State

   화면에 표시할 내용을 BoxState 로 정의합니다. 이 앱의 경우 서버에서 데이터를 조회하여 표시하는 기능을 위해 다음과 같은 상태를 정의하였습니다.

   ```kotlin
   data class ExampleState(
       val onProgress: Boolean = false,
       val onError: Throwable? = false,
       val data: Data? = null
   ) : BoxState
   ```

   서버에서 데이터를 조회하는 동안은 `onProgress` 값을 `true` 로 설정하여 로딩중 화면을 그립니다. 서버 조회 후 data를 획득하면 data를 바탕으로 필요한 화면을 구성합니다. 만약 서버 조회에 실패 했다면 `onError` 값이 채워지고 이 경우 필요한 에러 화면을 노출하게 됩니다.

   

2. Event

   앱에는 화면의 목적에 따라 많은 이벤트가 있을 수 있습니다. 이 예제의 경우 데이터를 조회를 요청하는 이벤트, 조회 결과를 나타내는 이벤트, 조회된 데이터를 사용자가 선택하는 이벤트를 갖습니다.

   ```kotlin
   sealed class ExampleEvent : BoxEvent {
       object ReqeustData: ExampleEvent()
       data class FetchedData(val data: Data): ExampleEvent()
       data class OnError(val throwable: Throwable): ExampleEvent()
       object OnDataClicked: ExampleEvent()
   }
   ```

   

3. SideEffect

   SideEffect은 State에서 처리할 수 없는 비동기 작업이나 화면 전환 등을 처리할 수 있습니다. 이 앱의 경우 서버 데이터 조회와 사용자 이벤트 발생시 화면 전환을 SideEffect로 처리합니다.

   ```kotlin
   sealed class ExampleSideEffect : BoxSideEffect {
       object RequestData: ExampleSideEffect()
       object OnDataClicked: ExampleSideEffect()
   }
   ```

   

#### Vm과 View 정의

1. Vm

   Vm은 Box의 핵심이 되는 State, Event, SideEffect의 관계를 정의하는 Blueprint와 SideEffect를 수행하는 코드로 이뤄집니다. 다음 코드를 봐주세요.

   ```kotlin
   class ExampleVm : BoxVm<ExampleState, ExampleEvent, ExampleSideEffect>() {
   
       override val bluePrint: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect>
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
   
   fun ExampleVm.onCreatedBlueprint() 
   			: BoxBlueprint<ExampleState, ExampleEvent, ExampleSideEffect> {
       return bluePrint(ExampleState()) {
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

   - `Vm`은 해당 `Vm` 에서 처리할 State와 Event, SideEffect를 제네릭으로 선언합니다. 

   - `Vm` 의 Blueprint는 Vm의 `bluePrint()` 함수로 정의합니다. 이때 테스트 코드 작성의 편의를 위해 Vm 외부에 Vm의 확장 함수 형태로 정의하는 방법을 권장합니다.

   - Blueprint는 크게 Event 정의와, SideEffect 정의로 나눌 수 있습니다.

     - Event 정의
       - `on()` 함수와 함께 정의할 Event 타입을 제네렉으로 선언합니다.  
       - `on()` 함수의 코드 블록은 현재 State를 `this` 로, 정의된 Event를 `it` 으로 전달 받습니다. 
       - `on()` 함수는 `to()` 함수 구현으로 이 Event가 어떤 State로 변할지 또는 어떤 SideEffect가 발생해야 하는지 정의합니다. 
       - `to()` 함수는 새로 생성될 Event만 갖을 수도 있고 발생할 SideEffect만 갖을 수도 있습니다. 때론은 두 개의 값을 다 정의할 수도 있고 두개의 값을 다 갖지 않을 수도 있습니다. 두개의 값을 다 갖지 않을때는 아무런 동작을 하지 않습니다.
     - SideEffect의 정의 
       - SideEffect는 해당 SideEffect가 동작할 `Dispatchers` 에 따라서 3가지 함수로 선언할 수 있습니다.
         - `Dispatchers.Main`: Main Thread에서 동작할 SideEffect를 정의합니다. 다이얼로그나 팝업을 노출하거나 화면을 전환하는 등 이벤트를 처리하기에 적합합니다. `main()` 함수로 정의합니다.. 
         - `Dispatchers.Default`: Worker Thread 동작하기를 기대하는 SideEffect를 정의 합니다. API 통신을 처리하기 적합합니다. `background()` 함수로 정의합니다.
         - `Dispatchers.IO`: IO 작업을 처리하기 위해 사용합니다. Worker Thread에서 동작하지만 `Dispathcers.Default` 에 비하여 Priority 가 떨어집니다. `io()` 함수로 정의 합니다.

   - ExampleVm의 `bluePrint()` 코드를 살펴보면 아래와 같습니다.

     - `ExampleEvent.ReqeustData` 이벤트가 발생했을때 새로운 `ExampleState`는 이전 State를 카피하여 만들되 `onProgress` 상태를 true로 설정합니다. 동시에 SideEffect인 `ExampleSideEffect.RequestData` 를 발생시킵니다.
     - `ExampleSideEffect.RequestData` 가 발생했을때는 `Dispatchers.Default` 를 통해 `requestDataAsync()` 함수를 호출합니다. `requestDataAsync()` 의 결과 값은 Event로 함수가 종료되고 각각의 Event를 발생시킵니다. 
     - `ExampleEvent.FetchedData` 이벤트가 발생 했을때 새로운 `ExampleState`는 이전 State를 카피하여 만들되 `onProgress` 상태를 false로 설정, data를 api 결과로 받은 data로 설정합니다.
     - `ExampleEvent.OnError`  이벤트가 발생 했을때 새로운 `ExampleState`는 이전 State를 카피하여 만들되 `onProgress` 상태를 false로 설정, onError를 전달받은 error 객체로 설정합니다.
     - `ExampleEvent.OnDataClicked` 이벤트가 발생했을땐 새로운 State를 생성하지 않고 `ExampleSideEffect.OnDataClicked` 만 발생시킵니다. `ExampleSideEffect.OnDataClicked` 는 Main Thread에서 `moveToNextScreen()` 함수를 실행시켜 화면을 전환합니다.

     

2. View

   View는 `BoxActivity` 또는 `BoxFragment` 를 구현합니다. 이 예제에서는 `BoxActivity` 를 사용합니다. 아래 코드를 봐주세요.

   ```kotlin
   class ExampleActivity
       : BoxActivity<ExampleState, ExampleEvent, ExampleSideEffect>() {
   
       override val renderer: BoxRenderer<ExampleState, ExampleEvent>?
               = ExampleRenderer
       override val viewInitializer: BoxViewInitializer<ExampleState, ExampleEvent>?
               = Example
       override val layout: Int
               = R.layout.activity_example
   
       override val vm: ExampleVm by lazy {
           ViewModelProviders.of(this).get(ExampleVm::class.java)
       }
   }
   ```

   - Vm과 마찬가지로 BoxActivity도 해당 화면에서 취급할 State와 Event, SideEffect를 정의해야 합니다.

   - 화면에서 사용할 Vm을 정의합니다. BoxVm은 `AndroidViewModel` 을  사용하고 있어서 `ViewModelProviders.of().get()` 을 사용하여 선언 할 수 있습니다. 또는 dagger2 등 DI를 통하여 선언하는 것도 가능합니다.

   - View (`BoxActivity`/`BoxFragment`) 가 초기화 될때 1회 호출되는 `BoxViewInitializer` 를 정의합니다. `BoxViewInitializer`는 `BoxActivity`의 경우 `onCreate()` 시점에, `BoxFragment`의 경우 `onCreateView()` 시점에 호출됩니다. 초기화가 필요 없는 간단한 화면의 경우 생략할 수 있습니다. 이 예제에서는 화면에 진입하면 서버 데이터 조회를 수행하려고 합니다. 따라서 아래와 같은 형태로 구현할 수 있습니다.
   

     ```kotlin
    object ExampleInitView : BoxViewInitializer<ExampleState, ExampleEvent> {
         override fun <B : ViewDataBinding, VM : Vm> bindingVm(b: B?, vm: VM) {
             b.be<ActivityExampleBinding>().vm = vm
         }
     
         override fun initializeView(v: BoxAndroidView<ExampleState, ExampleEvent>
                                     , vm: Vm?) {
             vm.intent(ExampleEvent.RequestData)
         }
     
         override fun onCleared() {
             
         }
     }
     ```
   
   
     - xml에서 전달 받은 vm을 통하여 새로운 Event를 `intent()` 할 수 있도록 `bindingVm()`함수를 오버라이딩 하여  `vm` 값을 전달합니다.
     - `initializeView()` 에서 필요할 경우 뷰 초기화를 수행하고 (예: RecyclerView의 Adapter 설정 등) 최초 실행할 Event를 `vm.intent()` 를 통해 전달합니다.
   
   - 새로운 State가 발생했을때 View를 랜더링할 `BoxRenderer` 를 정의합니다. `Renderer` 의 구현은 다음 코드를 참고해주세요.
   
     ``` kotlin
     object ExampleRenderer : BoxRenderer<ExampleState, ExampleEvent> {
         override fun render(v: BoxAndroidView<ExampleState, ExampleEvent>, s: ExampleState, vm: Vm?) {
             val binding = v.binding<ActivityExampleBinding>()
             binding.onProgress = s.onProgress
             binding.data = s.data
             binding.onError = s.onError != null
         }
     }
     ```
     
     - Renderer는 화면을 그릴 `View`, 화면에 그려야하는 `State`, 그리고 새로운 `Event`를 전달할 `Vm`을 인자로 넘겨 받습니다. 이 예제의 경우 바인딩된 레이아웃에 State의 값을 넘기는 역할만 수행합니다. State에 따른 View 분기 처리는 레이아웃의 데이터 바인딩 기능을 통해 아래와 같은 형태로 그려집니다. 
     
     ```xml
     <?xml version="1.0" encoding="utf-8"?>
     <layout xmlns:android="http://schemas.android.com/apk/res/android"
         xmlns:app="http://schemas.android.com/apk/res-auto">
     
         <data>
     
             <variable
                 name="data"
                 type="com.mrt.box.sample.Data" />
     
             <variable
                 name="onError"
                 type="boolean" />
             
             <variable
                 name="onProgress"
                 type="boolean" />
         </data>
     
         <FrameLayout 
             xmlns:tools="http://schemas.android.com/tools"
             android:layout_width="match_parent"
             android:layout_height="match_parent"
             tools:context=".ExampleActivity">
     
             <TextView
                 android:id="@+id/label"
                 android:layout_width="match_parent"
                 android:layout_height="match_parent"
                 android:gravity="center"
                 android:text="@{data.name}"
                 android:visibility="@{data != null &amp;&amp; !onProgress &amp;&amp; onError == null}"
                 android:textSize="80sp"
                 android:onClick="@{(v) -> vm.intent(com.mrt.box.sample.ExampleEvent.OnDataClick.INSTANCE)}"/>
     
             <include
                 layout="@layout/layout_error" 
                 app:onError="@{onError}"/>
     
             <ProgressBar
                 android:layout_width="wrap_content"
                 android:layout_height="wrap_content"
                 android:layout_gravity="center"
                 android:visibility="@{onProgress}"
                 />
     
         </FrameLayout>
     </layout>
     ```
     
     - xml은 `Renderer`를 통해 전달 받은 데이터를 이용하여 View를 그립니다. 위 예제의 경우 `onProgress` 값을 사용하여 `ProgressBar` 를 표시하고, `onError` 데이터가 있을 경우 에러 페이지를 처리합니다. `onProgress`와 `onError` 값이 없으며 `data` 가 존재할 경우 `data.name` 을 `TextView` 에 표시하고 `TextView` 를 사용자가 탭했을 경우 `ExampleEvent.OnDataClick` 이벤트를 발생시켜 화면을 `Vm` 이 화면을 전환하도록 합니다.
     
     

#### 디버깅

Box는 불변하는 상태값이 단방향으로 흐르도록 설계되었습니다. 모든 이벤트는 `Vm`의 `intent()` 함수를 통해 전달되고 `intent()` 함수를 통해 생성된 새로운 상태는 `View`의 `render()` 함수에 전달되어 그려집니다. 앱을 개발하다 흔히 발생할 수 있는 에러 상황에서 확인해야할 포인트가 정해져 있기 때문에 복잡한 화면을 그릴때도 비교적 디버깅이 용이합니다.



#### Testing

Box는 `Vm` 에 정의된 Blueprint를 기반으로 동작합니다. 따라서 Blueprint가 의도한대로 동작한다면 화면이 제대로 동작한다고 간주 할 수 있습니다. `Vm` 으로 테스트 코드를 작성할때 아래 테스트 클래스를 확장하는 것을 추천합니다. (`BoxVm`의 테스트 코드를 작성할땐 `Mockito` 를 사용하여 mock 객체를 활용하는 것을 권장합니다.)

```kotlin
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


    protected fun doIoSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().ioWorkOrNull(output.sideEffect)!!(output.valid())
    }

    protected fun doHeavySideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().heavyWorkOrNull(output.sideEffect)!!(output.valid())
    }

    protected fun doSideEffect(output: BoxOutput.Valid<S, E, SE>) {
        mockBlueprint().workOrNull(output.sideEffect)!!(output.valid())
    }

    abstract fun mockBlueprint(): BoxBlueprint<S, E, SE>
}
```

위 테스트 클래스를 확장하여 `ExampleVm`의 테스트 코드를 작성하면 아래와 같은 형태가 됩니다.

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

