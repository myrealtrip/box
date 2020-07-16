package com.mrt.box.core

import com.mrt.box.android.BoxRenderingScope
import com.mrt.box.android.BoxVoidRenderingScope

/**
 * Created by jaehochoe on 16/07/2020.
 */
open class BoxMultipleScopeState : BoxState {
    override var scope: BoxRenderingScope? = BoxVoidRenderingScope
    private val scopes = mutableListOf<BoxRenderingScope>()

    fun <T : BoxMultipleScopeState> addScopes(vararg scope: BoxRenderingScope): T {
        scopes.addAll(scope)
        return this as T
    }

    fun scopes(): List<BoxRenderingScope> {
        return scopes
    }

    private fun clearScopes() {
        scopes.clear()
    }

    private fun clearScope() {
        scope = BoxVoidRenderingScope
    }

    fun clearAllScopes() {
        clearScopes()
        clearScope()
    }
}