package com.example.tennofreunde.data

import retrofit2.http.GET

interface GitHubRelicService {

    @GET("hahawasistlos1/TennoFreundeData/TennoFarm/relic_data.json")

    suspend fun getRelics():
            List<RelicEntry>
}