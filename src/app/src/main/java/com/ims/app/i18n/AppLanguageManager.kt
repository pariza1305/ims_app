package com.ims.app.i18n

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppLanguageManager {
    private const val PREFS_NAME = "ims_settings"
    private const val KEY_LANGUAGE_TAG = "language_tag"

    const val TAG_ENGLISH = "en"
    const val TAG_HINDI = "hi"
    const val TAG_PUNJABI = "pa"

    fun getSavedLanguageTag(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE_TAG, TAG_ENGLISH) ?: TAG_ENGLISH
    }

    fun applySavedLanguage(context: Context) {
        applyLanguage(getSavedLanguageTag(context))
    }

    fun setLanguage(context: Context, languageTag: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE_TAG, languageTag).apply()
        applyLanguage(languageTag)
    }

    private fun applyLanguage(languageTag: String) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
    }
}
