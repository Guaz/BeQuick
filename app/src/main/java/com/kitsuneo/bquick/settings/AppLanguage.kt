package com.kitsuneo.bquick.settings

import androidx.annotation.StringRes
import com.kitsuneo.bquick.R
import java.util.Locale

enum class AppLanguage(
    val languageTag: String,
    @StringRes val labelRes: Int
) {
    English("en", R.string.language_english),
    Polish("pl", R.string.language_polish);

    companion object {
        fun fromLanguageTag(languageTag: String?): AppLanguage? {
            if (languageTag.isNullOrBlank()) return null
            val normalized = Locale.forLanguageTag(languageTag).language
            return entries.firstOrNull { it.languageTag == normalized }
        }

        fun fromDeviceLocale(locale: Locale = Locale.getDefault()): AppLanguage = when (locale.language) {
            Polish.languageTag -> Polish
            else -> English
        }
    }
}
