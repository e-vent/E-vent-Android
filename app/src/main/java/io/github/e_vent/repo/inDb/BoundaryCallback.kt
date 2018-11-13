/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.e_vent.repo.inDb

import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import androidx.annotation.MainThread
import io.github.e_vent.api.RedditApi
import io.github.e_vent.util.createStatusLiveData
import io.github.e_vent.vo.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * This boundary callback gets notified when user reaches to the edges of the list such that the
 * database cannot provide any more data.
 * <p>
 * The boundary callback might be called multiple times for the same direction so it does its own
 * rate limiting using the PagingRequestHelper class.
 */
class BoundaryCallback(
        private val webservice: RedditApi,
        private val handleResponse: (RedditApi.ListingResponse?) -> Unit,
        private val ioExecutor: Executor,
        private val networkPageSize: Int)
    : PagedList.BoundaryCallback<Event>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            webservice.getTop(
                    limit = networkPageSize)
                    .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: Event) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            webservice.getTopAfter(
                    after = itemAtEnd.name,
                    limit = networkPageSize)
                    .enqueue(createWebserviceCallback(it))
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<RedditApi.ListingResponse>,
            it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            handleResponse(response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: Event) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : Callback<RedditApi.ListingResponse> {
        return object : Callback<RedditApi.ListingResponse> {
            override fun onFailure(
                    call: Call<RedditApi.ListingResponse>,
                    t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(
                    call: Call<RedditApi.ListingResponse>,
                    response: Response<RedditApi.ListingResponse>) {
                insertItemsIntoDb(response, it)
            }
        }
    }
}