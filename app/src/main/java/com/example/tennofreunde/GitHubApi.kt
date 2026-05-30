package com.example.tennofreunde.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GitHubApi {

    val api: GitHubRelicService by lazy {

        Retrofit.Builder()

            .baseUrl(
                "https://raw.githubusercontent.com/"
            )

            .addConverterFactory(
                GsonConverterFactory.create()
            )

            .build()

            .create(GitHubRelicService::class.java)
    }
}