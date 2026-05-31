package com.kitsuneo.bquick.settings

import android.content.Context
import android.media.ToneGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SoundTarget {
    ModeSwitch,
    Reaction
}

enum class BuiltInSound(val id: String, val label: String, val toneType: Int) {
    Pulse("pulse", "Pulse", ToneGenerator.TONE_PROP_BEEP),
    Bell("bell", "Bell", ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD),
    Chime("chime", "Chime", ToneGenerator.TONE_CDMA_PIP)
}

sealed interface SoundSelection {
    val label: String

    data class BuiltIn(val sound: BuiltInSound) : SoundSelection {
        override val label: String = sound.label
    }

    data class Custom(val uri: String, override val label: String) : SoundSelection
}

data class SoundSettings(
    val modeSwitch: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Pulse),
    val reaction: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Bell)
)

object SoundSettingsRepository {
    private const val PrefName = "sound_settings"
    private const val ModeSwitchKey = "mode_switch"
    private const val ModeSwitchLabelKey = "mode_switch_label"
    private const val ReactionKey = "reaction"
    private const val ReactionLabelKey = "reaction_label"
    private lateinit var appContext: Context
    private val _settings = MutableStateFlow(SoundSettings())
    val settings: StateFlow<SoundSettings> = _settings.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _settings.value = load()
    }

    fun updateBuiltIn(target: SoundTarget, sound: BuiltInSound) {
        val selection = SoundSelection.BuiltIn(sound)
        val updated = when (target) {
            SoundTarget.ModeSwitch -> _settings.value.copy(modeSwitch = selection)
            SoundTarget.Reaction -> _settings.value.copy(reaction = selection)
        }
        persist(updated)
    }

    fun updateCustom(target: SoundTarget, uri: String, label: String) {
        val selection = SoundSelection.Custom(uri = uri, label = label)
        val updated = when (target) {
            SoundTarget.ModeSwitch -> _settings.value.copy(modeSwitch = selection)
            SoundTarget.Reaction -> _settings.value.copy(reaction = selection)
        }
        persist(updated)
    }

    private fun load(): SoundSettings {
        if (!::appContext.isInitialized) return SoundSettings()
        val prefs = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        return SoundSettings(
            modeSwitch = decode(
                prefs.getString(ModeSwitchKey, null),
                prefs.getString(ModeSwitchLabelKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.Pulse)
            ),
            reaction = decode(
                prefs.getString(ReactionKey, null),
                prefs.getString(ReactionLabelKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.Bell)
            )
        )
    }

    private fun persist(settings: SoundSettings) {
        if (!::appContext.isInitialized) return
        _settings.value = settings
        val prefs = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(ModeSwitchKey, encode(settings.modeSwitch))
            .putString(ModeSwitchLabelKey, settings.modeSwitch.label)
            .putString(ReactionKey, encode(settings.reaction))
            .putString(ReactionLabelKey, settings.reaction.label)
            .apply()
    }

    private fun encode(selection: SoundSelection): String = when (selection) {
        is SoundSelection.BuiltIn -> "builtin:${selection.sound.id}"
        is SoundSelection.Custom -> "custom:${selection.uri}"
    }

    private fun decode(encoded: String?, label: String?, fallback: SoundSelection): SoundSelection {
        if (encoded.isNullOrBlank()) return fallback
        return when {
            encoded.startsWith("builtin:") -> {
                val id = encoded.substringAfter(':')
                val sound = BuiltInSound.entries.firstOrNull { it.id == id }
                if (sound != null) SoundSelection.BuiltIn(sound) else fallback
            }

            encoded.startsWith("custom:") -> {
                val uri = encoded.substringAfter(':')
                SoundSelection.Custom(uri = uri, label = label ?: "Selected media")
            }

            else -> fallback
        }
    }
}
