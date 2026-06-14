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
    val timerSignals: TimerSignalSettings = TimerSignalSettings(),
    val timerAlarmSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem),
    val defaultAlarmSound: SoundSelection = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem),
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
    private const val IntervalStartSignalKey = "interval_start_signal"
    private const val IntervalWorkSignalKey = "interval_work_signal"
    private const val IntervalRestSignalKey = "interval_rest_signal"
    private const val RandomStartSignalKey = "random_start_signal"
    private const val RandomBeepSignalKey = "random_beep_signal"
    private const val TimerAlarmSoundKey = "timer_alarm_sound"
    private const val TimerAlarmSoundLabelKey = "timer_alarm_sound_label"
    private const val DefaultAlarmSoundKey = "default_alarm_sound"
    private const val DefaultAlarmSoundLabelKey = "default_alarm_sound_label"
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

    fun updateTimerSignal(target: TimerSignalPickerTarget, signal: TimerSignal) {
        val timerSignals = _settings.value.timerSignals
        val updatedSignals = when (target) {
            TimerSignalPickerTarget.IntervalStart -> timerSignals.copy(intervalStart = signal)
            TimerSignalPickerTarget.IntervalWork -> timerSignals.copy(intervalWork = signal)
            TimerSignalPickerTarget.IntervalRest -> timerSignals.copy(intervalRest = signal)
            TimerSignalPickerTarget.RandomStart -> timerSignals.copy(randomStart = signal)
            TimerSignalPickerTarget.RandomBeep -> timerSignals.copy(randomBeep = signal)
            is TimerSignalPickerTarget.IntervalExtraCueTarget -> timerSignals.copy(
                intervalExtraCues = timerSignals.intervalExtraCues.toMutableList().apply {
                    this[target.index] = this[target.index].copy(signal = signal)
                }
            ).normalized()
        }
        persist(_settings.value.copy(timerSignals = updatedSignals))
    }

    fun updateIntervalExtraCueEnabled(index: Int, enabled: Boolean) {
        updateIntervalExtraCue(index) { it.copy(enabled = enabled) }
    }

    fun updateIntervalExtraCueMode(index: Int, mode: IntervalCueMode) {
        updateIntervalExtraCue(index) { it.copy(mode = mode) }
    }

    fun updateIntervalExtraCueSeconds(index: Int, secondsBeforeEnd: Int) {
        updateIntervalExtraCue(index) { it.copy(secondsBeforeEnd = secondsBeforeEnd.coerceIn(1, 3599)) }
    }

    fun updateDefaultAlarmSound(selection: SoundSelection) {
        persist(_settings.value.copy(defaultAlarmSound = selection))
    }

    fun updateTimerAlarmSound(selection: SoundSelection) {
        persist(_settings.value.copy(timerAlarmSound = selection))
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
            timerSignals = TimerSignalSettings(
                intervalStart = TimerSignal.fromId(prefs.getString(IntervalStartSignalKey, null))
                    ?: TimerSignalSettings().intervalStart,
                intervalWork = TimerSignal.fromId(prefs.getString(IntervalWorkSignalKey, null))
                    ?: TimerSignalSettings().intervalWork,
                intervalRest = TimerSignal.fromId(prefs.getString(IntervalRestSignalKey, null))
                    ?: TimerSignalSettings().intervalRest,
                randomStart = TimerSignal.fromId(prefs.getString(RandomStartSignalKey, null))
                    ?: TimerSignalSettings().randomStart,
                randomBeep = TimerSignal.fromId(prefs.getString(RandomBeepSignalKey, null))
                    ?: TimerSignalSettings().randomBeep,
                intervalExtraCues = buildList {
                    repeat(TimerSignalSettings.MaxIntervalExtraCues) { index ->
                        add(
                            IntervalExtraCue(
                                enabled = prefs.getBoolean("interval_extra_${index}_enabled", false),
                                mode = prefs.getString(
                                    "interval_extra_${index}_mode",
                                    IntervalCueMode.SecondsBeforeEnd.name
                                )?.let { saved ->
                                    IntervalCueMode.entries.firstOrNull { it.name == saved }
                                } ?: IntervalCueMode.SecondsBeforeEnd,
                                secondsBeforeEnd = prefs.getInt("interval_extra_${index}_seconds", 3),
                                signal = TimerSignal.fromId(prefs.getString("interval_extra_${index}_signal", null))
                                    ?: IntervalExtraCue().signal
                            )
                        )
                    }
                }
            ).normalized(),
            timerAlarmSound = SoundSelectionCodec.decode(
                prefs.getString(TimerAlarmSoundKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem),
                customLabel = prefs.getString(TimerAlarmSoundLabelKey, null)
            ),
            defaultAlarmSound = SoundSelectionCodec.decode(
                prefs.getString(DefaultAlarmSoundKey, null),
                fallback = SoundSelection.BuiltIn(BuiltInSound.WakeUpAnthem),
                customLabel = prefs.getString(DefaultAlarmSoundLabelKey, null)
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
        val normalized = settings.copy(timerSignals = settings.timerSignals.normalized())
        _settings.value = normalized
        appContext.getSharedPreferences(PrefName, Context.MODE_PRIVATE).edit {
            putString(IntervalStartSignalKey, normalized.timerSignals.intervalStart.id)
            putString(IntervalWorkSignalKey, normalized.timerSignals.intervalWork.id)
            putString(IntervalRestSignalKey, normalized.timerSignals.intervalRest.id)
            putString(RandomStartSignalKey, normalized.timerSignals.randomStart.id)
            putString(RandomBeepSignalKey, normalized.timerSignals.randomBeep.id)
            putString(TimerAlarmSoundKey, SoundSelectionCodec.encode(normalized.timerAlarmSound))
            putString(TimerAlarmSoundLabelKey, normalized.timerAlarmSound.label)
            putString(DefaultAlarmSoundKey, SoundSelectionCodec.encode(normalized.defaultAlarmSound))
            putString(DefaultAlarmSoundLabelKey, normalized.defaultAlarmSound.label)
            normalized.timerSignals.intervalExtraCues.forEachIndexed { index, cue ->
                putBoolean("interval_extra_${index}_enabled", cue.enabled)
                putString("interval_extra_${index}_mode", cue.mode.name)
                putInt("interval_extra_${index}_seconds", cue.secondsBeforeEnd)
                putString("interval_extra_${index}_signal", cue.signal.id)
            }
            putString(
                HomeOrderKey,
                normalizeHomeOrder(normalized.homeOrder).joinToString(separator = ",") { it.name }
            )
            putString(AlarmTimeFormatKey, normalized.alarmTimeFormat.name)
            putString(AppLanguageKey, normalized.appLanguage.languageTag)
        }
    }

    private fun updateIntervalExtraCue(index: Int, transform: (IntervalExtraCue) -> IntervalExtraCue) {
        if (index !in 0 until TimerSignalSettings.MaxIntervalExtraCues) return
        val cues = _settings.value.timerSignals.intervalExtraCues.toMutableList()
        cues[index] = transform(cues[index])
        persist(
            _settings.value.copy(
                timerSignals = _settings.value.timerSignals.copy(intervalExtraCues = cues).normalized()
            )
        )
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
