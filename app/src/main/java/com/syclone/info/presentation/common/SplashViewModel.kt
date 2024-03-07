package com.syclone.info.presentation.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com. syclone.info.presentation.util.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class SplashViewModel @Inject constructor(
    ) : ViewModel() {

    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination: MutableState<String> = mutableStateOf(Screen.Onboarding.route)
    val startDestination: State<String> = _startDestination

    init {
        _startDestination.value = com.syclone.info.presentation.util.Screen.Home.route
        _isLoading.value = false


    }
}