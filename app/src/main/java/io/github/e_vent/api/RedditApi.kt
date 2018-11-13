package io.github.e_vent.api

import android.util.Log
import io.github.e_vent.vo.Event
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API communication setup
 */
interface RedditApi {
    @GET("/r/androiddev/hot.json")
    fun getTop(
            @Query("limit") limit: Int): Call<ListingResponse>

    // for after/before param, either get from RedditDataResponse.after/before,
    // or pass RedditNewsDataResponse.name (though this is technically incorrect)
    @GET("/r/androiddev/hot.json")
    fun getTopAfter(
            @Query("after") after: String,
            @Query("limit") limit: Int): Call<ListingResponse>

    class ListingResponse(val data: ListingData)

    class ListingData(
            val children: List<RedditChildrenResponse>
    )

    data class RedditChildrenResponse(val data: Event)

    companion object {
        private const val BASE_URL = "https://www.reddit.com/"
        fun create(): RedditApi = create(HttpUrl.parse(BASE_URL)!!)
        fun create(httpUrl: HttpUrl): RedditApi {
            val logger = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger {
                Log.d("API", it)
            })
            logger.level = HttpLoggingInterceptor.Level.BASIC

            val client = OkHttpClient.Builder()
                    .addInterceptor(logger)
                    .build()
            return Retrofit.Builder()
                    .baseUrl(httpUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(RedditApi::class.java)
        }
    }
}