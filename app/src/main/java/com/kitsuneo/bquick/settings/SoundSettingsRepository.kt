package com.kitsuneo.bquick.settings

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.core.content.edit
import com.kitsuneo.bquick.R
import com.kitsuneo.bquick.feature.home.HomeDestination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class SoundTarget {
    ModeSwitch,
    Reaction
}

enum class BuiltInSound(
    val id: String,
    val defaultLabel: String,
    @StringRes val labelRes: Int,
    @RawRes val rawResId: Int
) {
    EnergizedSunrise(
        "energized_sunrise",
        "Energized Sunrise",
        R.string.sound_energized_sunrise,
        R.raw.energized_sunrise
    ),
    WakeUpAnthem(
        "wake_up_anthem",
        "Wake Up Anthem",
        R.string.sound_wake_up_anthem,
        R.raw.wake_up_anthem
    ),
    RelaxedSunrise(
        "relaxed_sunrise",
        "Relaxed Sunrise",
        R.string.sound_relaxed_sunrise,
        R.raw.relaxed_sunrise
    ),
    CalmWeekend(
        "calm_weekend",
        "Calm Weekend",
        R.string.sound_calm_weekend,
        R.raw.calm_weekend
    ),
    TimeToShine(
        "time_to_shine",
        "Time To Shine",
        R.string.sound_time_to_shine,
        R.raw.time_to_shine
    ),
    SmoothMorning(
        "smooth_morning",
        "Smooth Morning",
        R.string.sound_smooth_morning,
        R.raw.smooth_morning
    )
}

sealed interface SoundSelection {
    val label: String

    data class BuiltIn(val sound: BuiltInSound) : SoundSelection {
        override val label: String = sound.defaultLabel
    }

    data class Custom(val uri: String, override val label: String) : SoundSelection
}

data class SoundSettings(
    val modeSwitch: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.SmoothMorning),
    val reaction: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.TimeToShine),
    val homeOrder: List<HomeDestination> = HomeDestination.entries,
    val alarmTimeFormat: AlarmTimeFormat = AlarmTimeFormat.Hours24,
    val appLanguage: AppLanguage = AppLanguage.English
)

object SoundSelectionCodec {
    fun encode(selection: SoundSelection): String = when (selection) {
        is SoundSelection.BuiltIn -> "builtin:${selection.sound.id}"
        is SoundSelection.Custom -> "custom:${selection.uri}|${selection.label}"
    }

    fun decode(
        encoded: String?,
        fallback: SoundSelection,
        customLabel: String? = null
    ): SoundSelection {
        if (encoded.isNullOrBlank()) return fallback
        return when {
            encoded.startsWith("builtin:") -> {
                val id = encoded.substringAfter(':')
                val sound = BuiltInSound.entries.firstOrNull { it.id == id }
                if (sound != null) SoundSelection.BuiltIn(sound) else fallback
            }

            encoded.startsWith("custom:") -> {
                val payload = encoded.substringAfter(':')
                val separatorIndex = payload.indexOf('|')
                if (separatorIndex == -1) {
                    SoundSelection.Custom(payload, customLabel ?: fallback.label)
                } else {
                    SoundSelection.Custom(
                        uri = payload.substring(0, separatorIndex),
                        label = payload.substring(separatorIndex + 1)
                    )
                }
            }

            else -> fallback
        }
    }
}

object SoundLibraryRepository {
    private const val PrefName = "sound_library"
    private const val CustomSoundsKey = "custom_sounds"

    private lateinit var appContext: Context
    private val _customSounds = MutableStateFlow<List<SoundSelection.Custom>>(emptyList())
    val customSounds: StateFlow<List<SoundSelection.Custom>> = _customSounds.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _customSounds.value = load()
    }

    fun addCustomSound(uri: String, label: String): SoundSelection.Custom {
        val sound = SoundSelection.Custom(uri = uri, label = label)
        val updated = buildList {
            add(sound)
            addAll(_customSounds.value.filterNot { it.uri == uri })
        }
        persist(updated)
        return sound
    }

    private fun load(): List<SoundSelection.Custom> {
        if (!::appContext.isInitialized) return emptyList()
        val prefs = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        val encoded = prefs.getStringSet(CustomSoundsKey, emptySet()).orEmpty()
        return encoded.mapNotNull { item ->
            SoundSelectionCodec.decode(item, fallback = SoundSelection.Custom("", "")).let { selection ->
                selection as? SoundSelection.Custom
            }?.takeIf { it.uri.isNotBlank() && it.label.isNotBlank() }
        }
    }

    private fun persist(sounds: List<SoundSelection.Custom>) {
        if (!::appContext.isInitialized) return
        _customSounds.value = sounds
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit {
            putStringSet(
                CustomSoundsKey,
                sounds.map(SoundSelectionCodec::encode).toSet()
            )
        }
    }
}

object SoundSettingsRepository {
    private const val PrefName = "sound_settings"
    private const val ModeSwitchKey = "mode_switch"
    private const val ModeSwitchLabelKey = "mode_switch_label"
    private const val ReactionKey = "reaction"
    private const val ReactionLabelKey = "reaction_label"
    private const val HomeOrderKey = "home_order"
    private const val AlarmTimeFormatKey = "alarm_time_format"
    private const val AppLanguageKey = "app_language"
    private lateinit var appContext: Context
    private val _settings = MutableStateFlow(SoundSettings())
    val settings: StateFlow<SoundSettings> = _settings.asStateFlow()

    fun initialize(context: Context) {
        appContext = context.applicationContext
        _settings.value = load()
        applyAppLanguage(appContext, _settings.value.appLanguage)
    }

    fun applyStoredAppLanguage(context: Context) {
        applyAppLanguage(context, resolveLanguage(context))
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

    fun updateHomeOrder(order: List<HomeDestination>) {
        persist(_settings.value.copy(homeOrder = normalizeHomeOrder(order)))
    }

    fun updateAppLanguage(language: AppLanguage) {
        val updated = _settings.value.copy(appLanguage = language)
        persist(updated)
        if (::appContext.isInitialized) {
            applyAppLanguage(appContext, language)
        }
    }

    private fun load(): SoundSettings {
        if (!::appContext.isInitialized) return SoundSettings()
        val prefs = appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        return SoundSettings(
            modeSwitch = SoundSelectionCodec.decode(
                prefs.getString(ModeSwitchKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.SmoothMorning),
                customLabel = prefs.getString(ModeSwitchLabelKey, null)
            ),
            reaction = SoundSelectionCodec.decode(
                prefs.getString(ReactionKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.TimeToShine),
                customLabel = prefs.getString(ReactionLabelKey, null)
            ),
            homeOrder = decodeHomeOrder(prefs.getString(HomeOrderKey, null)),
            alarmTimeFormat = AlarmTimeFormat.entries.firstOrNull {
                it.name == prefs.getString(AlarmTimeFormatKey, AlarmTimeFormat.Hours24.name)
            } ?: AlarmTimeFormat.Hours24,
            appLanguage = resolveLanguage(appContext)
        )
    }

    private fun persist(settings: SoundSettings) {
        if (!::appContext.isInitialized) return
        _settings.value = settings
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit {
            putString(ModeSwitchKey, SoundSelectionCodec.encode(settings.modeSwitch))
            putString(ModeSwitchLabelKey, settings.modeSwitch.label)
            putString(ReactionKey, SoundSelectionCodec.encode(settings.reaction))
            putString(ReactionLabelKey, settings.reaction.label)
            putString(
                HomeOrderKey,
                normalizeHomeOrder(settings.homeOrder).joinToString(separator = ",") { it.name }
            )
            putString(AlarmTimeFormatKey, settings.alarmTimeFormat.name)
            putString(AppLanguageKey, settings.appLanguage.languageTag)
        }
    }

    private fun decodeHomeOrder(encoded: String?): List<HomeDestination> {
        if (encoded.isNullOrBlank()) return HomeDestination.entries
        val parsed = encoded
            .split(',')
            .mapNotNull { token -> HomeDestination.entries.firstOrNull { it.name == token } }
        return normalizeHomeOrder(parsed)
    }

    private fun normalizeHomeOrder(order: List<HomeDestination>): List<HomeDestination> {
        val normalized = order.distinct().toMutableList()
        HomeDestination.entries.forEach { destination ->
            if (destination !in normalized) {
                normalized += destination
            }
        }
        return normalized
    }

    private fun resolveLanguage(context: Context): AppLanguage {
        val prefs = context.applicationContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE)
        return AppLanguage.fromLanguageTag(prefs.getString(AppLanguageKey, null))
            ?: AppLanguage.fromDeviceLocale()
    }

    private fun applyAppLanguage(context: Context, language: AppLanguage) {
        val locale = Locale.forLanguageTag(language.languageTag)
        Locale.setDefault(locale)
        updateResourcesConfiguration(context, locale)
        if (context.applicationContext !== context) {
            updateResourcesConfiguration(context.applicationContext, locale)
        }
    }

    private fun updateResourcesConfiguration(context: Context, locale: Locale) {
        val resources = context.resources
        val configuration = Configuration(resources.configuration).apply {
            setLocale(locale)
            setLocales(LocaleList(locale))
            setLayoutDirection(locale)
        }
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}

fun SoundSelection.displayLabel(context: Context): String = when (this) {
    is SoundSelection.BuiltIn -> context.getString(sound.labelRes)
    is SoundSelection.Custom -> label
}
