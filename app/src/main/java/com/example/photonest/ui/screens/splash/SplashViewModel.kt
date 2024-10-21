package com.example.photonest.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SplashViewModel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Unknown)
    val loginState: StateFlow<LoginState> = _loginState

    init {
        checkLoginState()
    }

    private fun checkLoginState() {
        viewModelScope.launch {
            var isLoggedIn = false
            val currentUser = FirebaseAuth.getInstance().currentUser
            _loginState.value = if (currentUser != null) {
                isLoggedIn = true
                LoginState.LoggedIn
            } else {
                LoginState.NotLoggedIn
            }
            _loginState.value = if (isLoggedIn) LoginState.LoggedIn else LoginState.NotLoggedIn
        }
    }
}

sealed class LoginState {
    object Unknown : LoginState()
    object LoggedIn : LoginState()
    object NotLoggedIn : LoginState()
}