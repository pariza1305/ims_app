package com.ims.app.data

import android.content.Context
import com.ims.app.data.entity.EvaluationMethod

object AppSettingsManager {
    private const val PREFS_NAME = "ims_settings"
    private const val KEY_GRADING_SYSTEM = "grading_system"
    private const val KEY_AUTO_UNIQUE_ID = "auto_unique_id"

    fun getSavedGradingSystem(context: Context): EvaluationMethod {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_GRADING_SYSTEM, EvaluationMethod.GPA.name) ?: EvaluationMethod.GPA.name
        return try {
            EvaluationMethod.valueOf(savedName)
        } catch (e: Exception) {
            EvaluationMethod.GPA
        }
    }

    fun setSavedGradingSystem(context: Context, method: EvaluationMethod) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_GRADING_SYSTEM, method.name).apply()
    }

    fun getAutoUniqueId(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_AUTO_UNIQUE_ID, true)
    }

    fun setAutoUniqueId(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_AUTO_UNIQUE_ID, enabled).apply()
    }
}
