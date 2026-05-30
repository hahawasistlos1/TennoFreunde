package com.example.tennofreunde.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import com.example.tennofreunde.api.PrimeDropResponse
interface WarframeApi {

    @GET("pc/voidTrader")

    suspend fun getBaro(): BaroResponse

    @GET("pc/alerts")

    suspend fun getAlerts(): List<AlertResponse>

    @GET("pc/fissures")

    suspend fun getFissures(): List<FissureResponse>

    @GET("pc/events")

    suspend fun getEvents(): List<EventResponse>

    @GET("prime-drops")

    suspend fun getPrimeDrops():
            List<PrimeDropResponse>

    companion object {

        val api: WarframeApi by lazy {

            Retrofit.Builder()

                .baseUrl("https://api.warframestat.us/")

                .addConverterFactory(
                    GsonConverterFactory.create()
                )

                .build()

                .create(WarframeApi::class.java)
        }
    }
}