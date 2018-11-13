package io.github.e_vent.api

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

interface CallbackLite<T> : Callback<T> {
    fun onResponse(response: Response<T>)

    fun onFailure(t: Throwable)

    override fun onResponse(call: Call<T>, response: Response<T>) {
        onResponse(response)
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        onFailure(t)
    }
}
