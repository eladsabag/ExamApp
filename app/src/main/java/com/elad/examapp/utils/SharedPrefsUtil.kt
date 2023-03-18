package com.elad.examapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.internal.Primitives
import com.google.gson.reflect.TypeToken
import java.util.*
import kotlin.collections.HashMap

/**
 * Created by Elad Sabag on 14/9/22
 */
class SharedPrefsUtil private constructor(context: Context) {
    private val prefs: SharedPreferences

    init {
        prefs = context.applicationContext.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun putIntToSP(key: String?, value: Int) {
        val editor = prefs.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    fun getIntFromSP(key: String?, def: Int): Int {
        return prefs.getInt(key, def)
    }

    fun putStringToSP(key: String?, value: String?) {
        val editor = prefs.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun getStringFromSP(key: String?, def: String?): String? {
        return prefs.getString(key, def)
    }

    fun putBooleanToSP(key: String?, value: Boolean) {
        val editor = prefs.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun getBooleanFromSP(key: String?, def: Boolean): Boolean {
        return prefs.getBoolean(key, def)
    }

    fun <T> putArrayToSP(KEY: String?, array: ArrayList<T>?) {
        val json: String = Gson().toJson(array)
        prefs.edit().putString(KEY, json).apply()
    }

    fun <T> getArrayFromSP(KEY: String?, typeToken: TypeToken<T>): ArrayList<T> {
        try {
            return Gson().fromJson(prefs.getString(KEY, ""), typeToken.type)
                ?: return ArrayList()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return ArrayList()
    }

    fun <S, T> putMapToSP(KEY: String?, map: HashMap<S, T>?) {
        val json: String = Gson().toJson(map)
        prefs.edit().putString(KEY, json).apply()
    }

    fun <S, T> getMapFromSP(KEY: String?, typeToken: TypeToken<T>): HashMap<S, T> {
        try {
            return Gson().fromJson(prefs.getString(KEY, ""), typeToken.type)
                ?: return HashMap()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return HashMap()
    }

    fun putDoubleToSP(KEY: String?, defValue: Double) {
        putStringToSP(KEY, defValue.toString())
    }

    fun getDoubleFromSP(KEY: String?, defValue: Double): Double {
        return getStringFromSP(KEY, defValue.toString())?.toDouble() ?: defValue
    }

    fun putObjectToSP(KEY: String?, value: Any?) {
        prefs.edit().putString(KEY, Gson().toJson(value)).apply()
    }

    fun <T> getObjectFromSP(KEY: String?, mModelClass: Class<T>?): T? {
        var obj: Any? = null
        try {
            obj = Gson().fromJson(prefs.getString(KEY, ""), mModelClass)
        } catch (ex: java.lang.Exception) {
            ex.printStackTrace()
        }
        return Primitives.wrap(mModelClass).cast(obj)
    }

    fun clearSP() = prefs.edit().clear().apply()

    fun getAllFromSP(): Map<String, *> = prefs.all

    companion object {
        const val MY_PREFS_NAME = "MY_PREFS_NAME"
        private var instance: SharedPrefsUtil? = null

        fun getInstance(context: Context) : SharedPrefsUtil? {
            if (instance == null) {
                instance = SharedPrefsUtil(context)
            }
            return instance
        }
    }
}