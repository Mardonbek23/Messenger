package com.example.messenger.shared

import android.content.Context
import android.content.SharedPreferences

class SharedPreference {
    private val APP_SETTINGS = "APP_SETTINGS"
    private val SOME_STRING_VALUE = "SOME_STRING_VALUE"

    private fun SharedPreference() {}

    private fun getSharedPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(APP_SETTINGS, Context.MODE_PRIVATE)
    }

    fun getSomeStringValue(context: Context): String? {
        return getSharedPreferences(context).getString(SOME_STRING_VALUE, null)
    }

    fun setSomeStringValue(context: Context, newValue: String?) {
        val editor = getSharedPreferences(context).edit()
        editor.putString(SOME_STRING_VALUE, newValue)
        editor.commit()
    }
    fun getEnabledItem(context: Context): Int? {
        return getSharedPreferences(context).getInt("key",0)
    }

    fun setEnabledItem(context: Context, newValue: Int?) {
        val editor = getSharedPreferences(context).edit()
        editor.putInt("key", newValue!!)
        editor.commit()
    }

}