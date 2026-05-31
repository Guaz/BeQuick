package com.kitsuneo.bquick.settings

import android.content.Context
import android.media.ToneGenerator
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.kitsuneo.bquick.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class SoundTarget {
    ModeSwitch,
    Reaction
}

enum class BuiltInSound(
    val id: String,
    val defaultLabel: String,
    @StringRes val labelRes: Int,
    val toneType: Int
) {
    Pulse("pulse", "Pulse", R.string.sound_pulse, ToneGenerator.TONE_PROP_BEEP),
    Bell("bell", "Bell", R.string.sound_bell, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD),
    Chime("chime", "Chime", R.string.sound_chime, ToneGenerator.TONE_CDMA_PIP)
}

sealed interface SoundSelection {
    val label: String

    data class BuiltIn(val sound: BuiltInSound) : SoundSelection {
        override val label: String = sound.defaultLabel
    }

    data class Custom(val uri: String, override val label: String) : SoundSelection
}

data class SoundSettings(
    val modeSwitch: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Pulse),
    val reaction: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.Bell),
    val alarmTimeFormat: AlarmTimeFormat = AlarmTimeFormat.Hours24,
    val appLanguage: AppLanguage = AppLanguage.English
)

object SoundSettingsRepository {
    private const val PrefName = "sound_settings"
    private const val ModeSwitchKey = "mode_switch"
    private const val ModeSwitchLabelKey = "mode_switch_label"
    private const val ReactionKey = "reaction"
    private const val ReactionLabelKey = "reaction_label"
    private const val AlarmTimeFormatKey = "alarm_time_format"
    private const val AppLanguageKey = "app_language"
    private lateinit var appContext: Context
    private val _settings = MutableStateFlow(SoundSettings())
    val settings: StateFlow<SoundSettings> = _settings.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _settings.value = load()
        applyAppLanguage(_settings.value.appLanguage)
    }

    fun applyStoredAppLanguage(context: Context) {
        applyAppLanguage(resolveLanguage(context))
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

    fun updateAlarmTimeFormat(format: AlarmTimeFormat) {
        persist(_settings.value.copy(alarmTimeFormat = format))
    }

    fun updateAppLanguage(language: AppLanguage) {
        val updated = _settings.value.copy(appLanguage = language)
        persist(updated)
        applyAppLanguage(language)
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
            ),
            alarmTimeFormat = AlarmTimeFormat.entries.firstOrNull {
                it.name == prefs.getString(AlarmTimeFormatKey, AlarmTimeFormat.Hours24.name)
            } ?: AlarmTimeFormat.Hours24,
            appLanguage = resolveLanguage(appContext)
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
            .putString(AlarmTimeFormatKey, settings.alarmTimeFormat.name)
            .putString(AppLanguageKey, settings.appLanguage.languageTag)
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
                SoundSelection.Custom(uri = uri, label = label ?: appContext.getString(R.string.selected_media))
            }

            else -> fallback
        }
    }

    private fun resolveLanguage(context: Context): AppLanguage {
        val prefs = context.applicationContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        return AppLanguage.fromLanguageTag(prefs.getString(AppLanguageKey, null))
            ?: AppLanguage.fromDeviceLocale()
    }

    private fun applyAppLanguage(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
    }
}

fun SoundSelection.displayLabel(context: Context): String = when (this) {
    is SoundSelection.BuiltIn -> context.getString(sound.labelRes)
    is SoundSelection.Custom -> label
}
