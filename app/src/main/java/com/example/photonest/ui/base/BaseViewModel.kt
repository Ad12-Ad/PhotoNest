package com.example.photonest.ui.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base ViewModel for MVI pattern.
 * S = State (what the UI displays)
 * I = Intent/Event (what the user does)
 * E = Effect (one-shot side effects: navigation, snackbar, share sheet)
 */
abstract class BaseViewModel<S, I, E>(initialState: S) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /** Called from UI when user performs an action. */
    abstract fun onIntent(intent: I)

    /** Update state — always thread-safe. */
    protected fun updateState(reduce: S.() -> S) {
        _state.update { it.reduce() }
    }

    /** Send a one-shot side effect to the UI. */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch { _effect.send(effect) }
    }

    /** Convenience: get current state. */
    protected val currentState: S get() = _state.value
}
