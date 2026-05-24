package com.project.store.utils

import android.content.Context
import android.content.Intent
import com.project.store.auth.LoginActivity
import com.project.store.data.repository.FirebaseRepository

object SessionManager {

    fun clearSession(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
    }

    fun logout(context: Context) {
        SecurePreferences.setJustLoggedOut(context, true)
        FirebaseRepository.getInstance().auth.signOut()
        clearSession(context)
        val intent = Intent(context, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(intent)
    }

    private const val PREFS_NAME = "tienda_virtual_session"
}
