package com.example.tennofreunde.data

import android.content.Context
import com.example.tennofreunde.api.PrimeDropResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrimeDropCache {

    private const val PREF_NAME =
        "prime_drop_cache"

    private const val KEY_DATA =
        "prime_drop_data"

    fun savePrimeDrops(

        context: Context,

        data: List<PrimeDropResponse>
    ) {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val json =
            Gson().toJson(data)

        prefs.edit()
            .putString(KEY_DATA, json)
            .apply()
    }

    fun loadPrimeDrops(
        context: Context
    ): List<PrimeDropResponse> {

        val prefs =
            context.getSharedPreferences(
                PREF_NAME,
                Context.MODE_PRIVATE
            )

        val json =
            prefs.getString(KEY_DATA, null)
                ?: return emptyList()

        val type =
            object :
                TypeToken<List<PrimeDropResponse>>() {}.type

        return Gson().fromJson(json, type)
    }
}