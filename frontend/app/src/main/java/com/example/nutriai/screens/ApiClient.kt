package com.example.nutriai.screens

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object ApiClient {
    private val logging = HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) }
    private val httpClient = OkHttpClient.Builder().addInterceptor(logging).build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://your.server.url/") // <-- Change this to your backend base URL!
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val nutriChatApi: NutriChatApiService = retrofit.create(NutriChatApiService::class.java)
}
