package io.github.e_vent.repo.inDb

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.annotation.MainThread
import androidx.paging.toLiveData
import io.github.e_vent.api.RedditApi
import io.github.e_vent.db.EventDb
import io.github.e_vent.repo.Listing
import io.github.e_vent.repo.NetworkState
import io.github.e_vent.repo.EventPostRepo
import io.github.e_vent.vo.Event
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.Executor

/**
 * Repository implementation that uses a database PagedList + a boundary callback to return a
 * listing that loads in pages.
 */
class DbRedditPostRepository(
        val db: EventDb,
        private val redditApi: RedditApi,
        private val ioExecutor: Executor,
        private val networkPageSize: Int = DEFAULT_NETWORK_PAGE_SIZE) : EventPostRepo {
    companion object {
        private const val DEFAULT_NETWORK_PAGE_SIZE = 10
    }

    /**
     * Inserts the response into the database while also assigning position indices to items.
     */
    private fun insertResultIntoDb(body: RedditApi.ListingResponse?) {
        body!!.data.children.let { posts ->
            db.runInTransaction {
                val start = db.posts().getNextIndex()
                val items = posts.mapIndexed { index, child ->
                    child.data.indexInResponse = start + index
                    child.data
                }
                db.posts().insert(items)
            }
        }
    }

    /**
     * When refresh is called, we simply run a fresh network request and when it arrives, clear
     * the database table and insert all new items in a transaction.
     * <p>
     * Since the PagedList already uses a database bound data source, it will automatically be
     * updated after the database transaction is finished.
     */
    @MainThread
    private fun refresh(): LiveData<NetworkState> {
        val networkState = MutableLiveData<NetworkState>()
        networkState.value = NetworkState.LOADING
        redditApi.getTop(networkPageSize).enqueue(
                object : Callback<RedditApi.ListingResponse> {
                    override fun onFailure(call: Call<RedditApi.ListingResponse>, t: Throwable) {
                        // retrofit calls this on main thread so safe to call set value
                        networkState.value = NetworkState.error(t.message)
                    }

                    override fun onResponse(
                            call: Call<RedditApi.ListingResponse>,
                            response: Response<RedditApi.ListingResponse>) {
                        ioExecutor.execute {
                            db.runInTransaction {
                                db.posts().delete()
                                insertResultIntoDb(response.body())
                            }
                            // since we are in bg thread now, post the result.
                            networkState.postValue(NetworkState.LOADED)
                        }
                    }
                }
        )
        return networkState
    }

    /**
     * Returns a Listing for the given subreddit.
     */
    @MainThread
    override fun posts(pageSize: Int): Listing<Event> {
        // create a boundary callback which will observe when the user reaches to the edges of
        // the list and update the database with extra data.
        val boundaryCallback = BoundaryCallback(
                webservice = redditApi,
                handleResponse = this::insertResultIntoDb,
                ioExecutor = ioExecutor,
                networkPageSize = networkPageSize)
        // we are using a mutable live data to trigger refresh requests which eventually calls
        // refresh method and gets a new live data. Each refresh request by the user becomes a newly
        // dispatched data in refreshTrigger
        val refreshTrigger = MutableLiveData<Unit>()
        val refreshState = Transformations.switchMap(refreshTrigger) {
            refresh()
        }

        // We use toLiveData Kotlin extension function here, you could also use LivePagedListBuilder
        val livePagedList = db.posts().posts().toLiveData(
                pageSize = pageSize,
                boundaryCallback = boundaryCallback)

        return Listing(
                pagedList = livePagedList,
                networkState = boundaryCallback.networkState,
                retry = {
                    boundaryCallback.helper.retryAllFailed()
                },
                refresh = {
                    refreshTrigger.value = null
                },
                refreshState = refreshState
        )
    }
}

