package com.xuan.life.android.data.remote

import android.content.Context
import android.content.SharedPreferences

class LifeTokenStore(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences("life_android_auth", Context.MODE_PRIVATE)

    fun getAccessToken(): String = preferences.getString(KEY_ACCESS_TOKEN, "") ?: ""

    fun getRefreshToken(): String = preferences.getString(KEY_REFRESH_TOKEN, "") ?: ""

    fun hasSession(): Boolean = getAccessToken().isNotBlank()

    fun save(tokens: TokenPairResponse) {
        preferences.edit()
            .putString(KEY_ACCESS_TOKEN, tokens.accessToken)
            .putString(KEY_REFRESH_TOKEN, tokens.refreshToken)
            .apply()
    }

    fun clear() {
        preferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    private companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}
