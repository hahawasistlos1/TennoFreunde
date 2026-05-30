package com.example.tennofreunde

import android.content.Context
import com.example.tennofreunde.data.RelicEntry
import org.json.JSONArray
import java.io.BufferedReader

object RelicLoader {

    fun loadRelics(

        context: Context

    ): Map<String, RelicEntry> {

        val jsonString = context.assets
            .open("relic_data.json")
            .bufferedReader()
            .use(BufferedReader::readText)

        val jsonArray = JSONArray(jsonString)

        val relicMap =
            mutableMapOf<String, RelicEntry>()

        for (i in 0 until jsonArray.length()) {

            val obj =
                jsonArray.getJSONObject(i)

            val part =
                obj.getString("part")

            val relic =
                obj.getString("relic")

            val rotation =
                obj.getString("rotation")

            val farmLocation =
                obj.getString("farmLocation")

            relicMap[
                part.lowercase()
            ] = RelicEntry(

                part = part,

                relic = relic,

                rotation = rotation,

                farmLocation = farmLocation
            )
        }

        return relicMap
    }
}