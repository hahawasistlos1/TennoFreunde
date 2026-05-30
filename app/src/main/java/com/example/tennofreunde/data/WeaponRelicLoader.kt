package com.example.tennofreunde.data

import android.content.Context
import org.json.JSONArray
import java.io.BufferedReader

object WeaponRelicLoader {

    fun loadWeapons(

        context: Context
    ): List<WeaponRelicEntry> {

        val jsonString = context.assets
            .open("weapon_relic_data.json")
            .bufferedReader()
            .use(BufferedReader::readText)

        val jsonArray = JSONArray(jsonString)

        val weapons = mutableListOf<WeaponRelicEntry>()

        for (i in 0 until jsonArray.length()) {

            val obj = jsonArray.getJSONObject(i)

            weapons.add(

                WeaponRelicEntry(

                    part = obj.getString("part"),

                    relic = obj.getString("relic"),

                    rotation = obj.getString("rotation"),

                    farmLocation = obj.getString("farmLocation")
                )
            )
        }

        return weapons
    }
}
