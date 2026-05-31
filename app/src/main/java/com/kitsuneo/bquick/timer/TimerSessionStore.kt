package com.kitsuneo.bquick.timer

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TimerSessionStore {
    private val _activeSession = MutableStateFlow<ActiveTimerSession?>(null)
    val activeSession: StateFlow<ActiveTimerSession?> = _activeSession.asStateFlow()

    fun update(session: ActiveTimerSession?) {
        _activeSession.value = session
    }
}
