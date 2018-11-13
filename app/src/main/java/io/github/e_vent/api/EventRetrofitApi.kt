package io.github.e_vent.api

import android.util.Log
import io.github.e_vent.vo.ServerEvent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * API communication setup
 */
interface EventRetrofitApi {
    @GET("/events/{id}")
    fun getEvent(@Path("id") id: Int): Call<ServerEvent>

    @GET("/count")
    fun getCount(): Call<Int>

    companion object {
        fun create(): EventRetrofitApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl("http://192.168.3.150:8000")
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(EventRetrofitApi::class.java)
        }
    }
}