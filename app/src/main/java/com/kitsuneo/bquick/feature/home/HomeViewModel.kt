package com.kitsuneo.bquick.feature.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeFeatureCard(
    val title: String,
    val eyebrow: String,
    val description: String,
    val primaryAction: String
)

data class HomeUiState(
    val headline: String = "Fast focus tools",
    val subheadline: String = "A Compose rewrite of the BFast concept with two training modes.",
    val features: List<HomeFeatureCard> = listOf(
        HomeFeatureCard(
            title = "Interval",
            eyebrow = "Structured timer",
            description = "Build work and rest rounds, then run the session with a live countdown.",
            primaryAction = "Open interval"
        ),
        HomeFeatureCard(
            title = "Random Sound Generator",
            eyebrow = "Cue drill",
            description = "Trigger short random beeps inside a timed session for reaction practice.",
            primaryAction = "Open random sound"
        )
    )
)

class HomeViewModel : ViewModel() {
    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()
}
