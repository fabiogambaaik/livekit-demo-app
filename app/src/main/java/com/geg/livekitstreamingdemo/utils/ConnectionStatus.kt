package com.geg.livekitstreamingdemo.utils

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object ConnectionStatus {
    private val _state = MutableStateFlow("")
    val state: StateFlow<String> = _state

    fun set(state: String) {
        _state.value = state
    }
}

