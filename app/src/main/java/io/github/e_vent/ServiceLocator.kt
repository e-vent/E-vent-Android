package io.github.e_vent

import android.app.Application
import android.content.Context
import io.github.e_vent.api.RedditApi
import io.github.e_vent.db.EventDb
import io.github.e_vent.repo.EventPostRepo
import io.github.e_vent.repo.inDb.DbRedditPostRepository
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Super simplified service locator implementation to allow us to replace default implementations
 * for testing.
 */
interface ServiceLocator {
    companion object {
        private val LOCK = Any()
        private var instance: ServiceLocator? = null
        fun instance(context: Context): ServiceLocator {
            synchronized(LOCK) {
                if (instance == null) {
                    instance = DefaultServiceLocator(
                            app = context.applicationContext as Application,
                            useInMemoryDb = false)
                }
                return instance!!
            }
        }
    }

    fun getRepository(): EventPostRepo

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getRedditApi(): RedditApi
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
open class DefaultServiceLocator(val app: Application, val useInMemoryDb: Boolean) : ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val db by lazy {
        EventDb.create(app, useInMemoryDb)
    }

    private val api by lazy {
        RedditApi.create()
    }

    override fun getRepository(): EventPostRepo {
        return DbRedditPostRepository(
                db = db,
                redditApi = getRedditApi(),
                ioExecutor = getDiskIOExecutor())
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getRedditApi(): RedditApi = api
}