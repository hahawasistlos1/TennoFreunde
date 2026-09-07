package com.example.tennofreunde.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import com.example.tennofreunde.api.PrimeDropResponse
interface WarframeApi {

    @GET("{platform}/voidTrader")

    suspend fun getBaro(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): BaroResponse

    @GET("{platform}/alerts")

    suspend fun getAlerts(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<AlertResponse>

    @GET("{platform}/fissures")

    suspend fun getFissures(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<FissureResponse>

    @GET("{platform}/events")

    suspend fun getEvents(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<EventResponse>

    @GET("{platform}/invasions")
    suspend fun getInvasions(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<InvasionResponse>

    @GET("{platform}/sortie")
    suspend fun getSortie(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): SortieResponse

    @GET("{platform}/cetusCycle")
    suspend fun getCetusCycle(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): CetusCycleResponse

    @GET("{platform}/vallisCycle")
    suspend fun getVallisCycle(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): CetusCycleResponse

    @GET("{platform}/earthCycle")
    suspend fun getEarthCycle(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): CetusCycleResponse

    @GET("{platform}/cambionCycle")
    suspend fun getCambionCycle(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): CetusCycleResponse

    @GET("{platform}/dailyDeals")
    suspend fun getDailyDeals(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<DailyDealResponse>

    @GET("{platform}/news")
    suspend fun getNews(@Path("platform") platform: String = "ps4", @Query("language") language: String = "de"): List<NewsResponse>

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
