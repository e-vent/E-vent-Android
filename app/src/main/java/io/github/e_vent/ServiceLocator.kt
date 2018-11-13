package io.github.e_vent

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import io.github.e_vent.api.EventRetrofitApi
import io.github.e_vent.db.EventDb
import io.github.e_vent.repo.EventPostRepo
import io.github.e_vent.repo.inDb.DbEventRepo
import io.github.e_vent.util.getServerAddrPref
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
                            app = context.applicationContext as Application)
                }
                return instance!!
            }
        }
    }

    fun getRepository(): EventPostRepo

    fun getNetworkExecutor(): Executor

    fun getDiskIOExecutor(): Executor

    fun getEventApi(): EventRetrofitApi

    fun refreshEventApi()
}

/**
 * default implementation of ServiceLocator that uses production endpoints.
 */
class DefaultServiceLocator(val app: Application) : ServiceLocator {
    // thread pool used for disk access
    @Suppress("PrivatePropertyName")
    private val DISK_IO = Executors.newSingleThreadExecutor()

    // thread pool used for network requests
    @Suppress("PrivatePropertyName")
    private val NETWORK_IO = Executors.newFixedThreadPool(5)

    private val SP = PreferenceManager.getDefaultSharedPreferences(app)
    private val PREF_ID_SERVER = app.getString(R.string.pref_id_server)
    private val SP_LISTENER = { _: SharedPreferences, key: String ->
        if (key == PREF_ID_SERVER) {
            refreshEventApi()
        }
    }

    private val db by lazy {
        EventDb.create(app)
    }

    private lateinit var api: EventRetrofitApi

    private var lastServerAddr: String? = null

    init {
        SP.registerOnSharedPreferenceChangeListener(SP_LISTENER)
        refreshEventApi()
    }

    override fun getRepository(): EventPostRepo {
        return DbEventRepo(
                db = db,
                serviceLocator = this,
                ioExecutor = getDiskIOExecutor())
    }

    override fun getNetworkExecutor(): Executor = NETWORK_IO

    override fun getDiskIOExecutor(): Executor = DISK_IO

    override fun getEventApi(): EventRetrofitApi = api

    override fun refreshEventApi() {
        val newAddr = getServerAddrPref(SP, app)
        if (newAddr == lastServerAddr) {
            return
        }
        if (lastServerAddr != null) {
            DISK_IO.execute {
                db.runInTransaction {
                    db.posts().delete()
                }
            }
        }
        api = EventRetrofitApi.create(newAddr)
        lastServerAddr = newAddr
    }
}