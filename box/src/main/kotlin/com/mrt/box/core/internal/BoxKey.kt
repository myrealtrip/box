package com.mrt.box.core.internal

/**
 * Created by jaehochoe on 2020-01-01.
 */
class BoxKey<T : Any, out R : T>(private val clazz: Class<R>) { //todo kclass
    private val matcher: (T) -> Boolean = { clazz.isInstance(it) }
    fun check(value: T) = matcher(value)
}