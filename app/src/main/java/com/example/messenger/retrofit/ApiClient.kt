package com.example.messenger.retrofit

import android.content.Context
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    const val Base_url="https://fcm.googleapis.com/"
    fun getRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    fun apiService(context: Context) = getRetrofit(context).create(ApiService::class.java)
}
