package com.mrt.box.core

/**
 * Created by jaehochoe on 2020-01-03.
 */
interface Vm {
    fun intent(event: Any): Any?
    fun intentIf(condition: Boolean, className: String, vararg arguments: Any): Any? {
        return if (condition)
            intent(className, arguments)
        else null
    }
    fun intent(className: String, vararg arguments: Any): Any? {
        return try {
            Class.forName(className)?.let { clazz ->
                try {
                    intent(clazz.constructors[0].newInstance(*arguments))
                } catch (e: Exception) {
                    try {
                        intent(
                            clazz.getConstructor(*arguments.map { it::class.java as Class<*> }
                                .toTypedArray()).newInstance(
                                *arguments
                            )
                        )
                    } catch (e: Exception) {
                        Box.log { e }
                    }
                }
            }
        } catch (e: Exception) {
            Box.log { e }
        }
    }
}