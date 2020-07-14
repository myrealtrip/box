package com.mrt.box.android

/**
 * Created by jaehochoe on 14/07/2020.
 */
open class BoxRenderingScope(val scope: String?) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BoxRenderingScope) return false
        if (scope != other.scope) return false
        return true
    }

    override fun hashCode(): Int {
        return scope?.hashCode() ?: 0
    }
}