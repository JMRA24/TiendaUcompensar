package com.project.store.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object SecurePreferences {

    private const val PREFS_NAME = "secure_prefs"
    private const val KEY_USER_ID = "last_user_id"
    private const val KEY_USER_ROLE = "last_user_role"
    private const val KEY_USER_NAME = "last_user_name"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_JUST_LOGGED_OUT = "just_logged_out"

    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.deleteSharedPreferences(PREFS_NAME)
            context.getSharedPreferences(PREFS_NAME + "_fallback", Context.MODE_PRIVATE)
        }
    }

    fun saveUserSession(context: Context, userId: String, role: String, name: String) {
        getPrefs(context).edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getUserId(context: Context): String? =
        getPrefs(context).getString(KEY_USER_ID, null)

    fun getUserRole(context: Context): String? =
        getPrefs(context).getString(KEY_USER_ROLE, null)

    fun getUserName(context: Context): String? =
        getPrefs(context).getString(KEY_USER_NAME, null)

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)

    fun setJustLoggedOut(context: Context, value: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_JUST_LOGGED_OUT, value).apply()
    }

    fun isJustLoggedOut(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_JUST_LOGGED_OUT, false)

    fun clear(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
