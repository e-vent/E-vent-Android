package io.github.e_vent.repo.inDb

import androidx.paging.PagedList
import androidx.paging.PagingRequestHelper
import androidx.annotation.MainThread
import io.github.e_vent.api.*
import io.github.e_vent.util.createStatusLiveData
import io.github.e_vent.vo.ClientEvent
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
        private val webservice: EventRetrofitApi,
        private val handleResponse: (ListingResponse?) -> Unit,
        private val ioExecutor: Executor)
    : PagedList.BoundaryCallback<ClientEvent>() {

    val helper = PagingRequestHelper(ioExecutor)
    val networkState = helper.createStatusLiveData()

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    @MainThread
    override fun onZeroItemsLoaded() {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.INITIAL) {
            doGetEvents(webservice, createWebserviceCallback(it))
        }
    }

    /**
     * User reached to the end of the list.
     */
    @MainThread
    override fun onItemAtEndLoaded(itemAtEnd: ClientEvent) {
        helper.runIfNotRunning(PagingRequestHelper.RequestType.AFTER) {
            doGetEvents(webservice, createWebserviceCallback(it), after = itemAtEnd.id + 1)
        }
    }

    /**
     * every time it gets new items, boundary callback simply inserts them into the database and
     * paging library takes care of refreshing the list if necessary.
     */
    private fun insertItemsIntoDb(
            response: Response<ListingResponse>,
            it: PagingRequestHelper.Request.Callback) {
        ioExecutor.execute {
            handleResponse(response.body())
            it.recordSuccess()
        }
    }

    override fun onItemAtFrontLoaded(itemAtFront: ClientEvent) {
        // ignored, since we only ever append to what's in the DB
    }

    private fun createWebserviceCallback(it: PagingRequestHelper.Request.Callback)
            : CallbackLite<ListingResponse> {
        return object : CallbackLite<ListingResponse> {
            override fun onFailure(t: Throwable) {
                it.recordFailure(t)
            }

            override fun onResponse(response: Response<ListingResponse>) {
                insertItemsIntoDb(response, it)
            }
        }
    }
}