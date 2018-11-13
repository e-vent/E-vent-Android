package io.github.e_vent.api

import android.util.Log
import io.github.e_vent.vo.ClientEvent
import io.github.e_vent.vo.ServerEvent
import retrofit2.Call
import retrofit2.Response
import kotlin.math.min

const val MAX_PAGE_SIZE = 30

data class ListingResponse(val data: List<ClientEvent>)

private class ResponseLogic(
        val callID: Int,
        val cb: CallbackLite<ListingResponse>,
        val calls: Array<Pair<Int, Call<ServerEvent>>?>,
        val buf: Array<ClientEvent?>,
        val donePtr: BooleanArray
): CallbackLite<ServerEvent> {
    override fun onFailure(t: Throwable) {
        Log.i("EventApi", "ResponseLogic.onFailure " + callID)
        if (!donePtr[0]) {
            synchronized(donePtr) {
                if (!donePtr[0]) {
                    if (callID == 0) {
                        // Not having a single item to return is a failure
                        cb.onFailure(t)
                        teardown()
                        // don't need to clear calls, we already tore down
                        Log.i("EventApi", "First request failed")
                        return
                    }
                    calls[callID] = null
                    for (call in calls.take(callID)) {
                        if (call != null) {
                            Log.i("EventApi", "Letting other call take care")
                            return
                        }
                    }
                    // All previous calls are done, will never care about the rest
                    teardown()
                    Log.i("EventApi", "Smaller successful response")
                    cb.onResponse(Response.success(ListingResponse(buf.map { it!! })))
                }
            }
        } else {
            Log.i("EventApi", "Ignoring failure, already done")
        }
    }
    private fun teardown() {
        donePtr[0] = true
        for (otherCall in calls) {
            otherCall!!.second.cancel()
        }
    }
    override fun onResponse(response: Response<ServerEvent>) {
        /*
         * We split into two cases, A. all successful B. otherwise.
         *
         * When everything's successful,
         * all except the last only add their response to the buffer.
         * The last one takes the buffer and sends it to the callback.
         *
         * When at least one fails,
         * responses trickle in until the failed one cancels them by setting donePtr.
         * The last one returns that (smaller than usual).
         */
        Log.i("EventApi", "ResponseLogic.onResponse: " + callID)
        if (response.isSuccessful) {
            if (!donePtr[0]) {
                synchronized(donePtr) {
                    if (!donePtr[0]) {
                        buf[callID] = response.body()!!.toClientEvent(callID)
                        calls[callID] = null
                        for (call in calls) {
                            if (call != null) {
                                Log.i("EventApi", "Have call still remaining")
                                for (call in calls) {
                                    Log.i("EventApi", "* " + call?.first + " : " + call?.second)
                                }
                                return
                            }
                        }
                        Log.i("EventApi", "All success")
                        // Don't need to set donePtr, because we checked we're the only call left
                        cb.onResponse(Response.success(ListingResponse(buf.map { it!! })))
                    }
                }
            } else {
                Log.i("EventApi", "Ignoring success, already done")
            }
        } else {
            onFailure(Throwable("Response did not succeed"))
        }
    }
}

fun doGetEvents(api: EventRetrofitApi, cb: CallbackLite<ListingResponse>, after: Int = 0) {
    api.getCount().enqueue(
            object : CallbackLite<Int> {
                override fun onFailure(t: Throwable) {
                    cb.onFailure(t)
                }
                override fun onResponse(response: Response<Int>) {
                    val stop = response.body()!!
                    val quantity = min(stop - after, MAX_PAGE_SIZE)
                    if (quantity <= 0) {
                        cb.onFailure(Throwable("Tried to get more than available"))
                        return
                    }
                    val calls = (0 until quantity).map {
                        Pair(it, api.getEvent(after + it))
                    }
                    val callsBuf: Array<Pair<Int, Call<ServerEvent>>?> = calls.toTypedArray()
                    val buf: Array<ClientEvent?> = arrayOfNulls(quantity)
                    val donePtr = booleanArrayOf(false)
                    for (call in calls) {
                        call.second.enqueue(ResponseLogic(call.first, cb, callsBuf, buf, donePtr))
                    }
                }
            }
    )
}