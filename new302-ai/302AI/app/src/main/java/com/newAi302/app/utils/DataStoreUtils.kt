package com.newAi302.app.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore


val Context.dataStore by preferencesDataStore(name = "proxy_info")

//key
val keyName = stringPreferencesKey("username")
val keyPassWord = stringPreferencesKey("password")
val keyPort = intPreferencesKey("port")
val keyType = stringPreferencesKey("type")
val keyServer = stringPreferencesKey("server")

val keyGoogleLogin = booleanPreferencesKey("isGoogleLogin")





