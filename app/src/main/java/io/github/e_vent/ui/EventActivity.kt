package io.github.e_vent.ui

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.e_vent.R
import io.github.e_vent.ServiceLocator
import io.github.e_vent.repo.NetworkState
import io.github.e_vent.vo.ClientEvent
import kotlinx.android.synthetic.main.activity_event.*

/**
 * A list activity that shows posts.
 * <p>
 * The intent arguments can be modified to make it use a different repository (see MainActivity).
 */
class EventActivity : AppCompatActivity() {
    companion object {
        fun intentFor(context: Context): Intent {
            val intent = Intent(context, EventActivity::class.java)
            return intent
        }
    }

    private lateinit var model: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_event)
        model = getViewModel()
        initAdapter()
        initSwipeToRefresh()
        model.show()
    }

    private fun getViewModel(): MyViewModel {
        return ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@EventActivity)
                        .getRepository()
                @Suppress("UNCHECKED_CAST")
                return MyViewModel(repo) as T
            }
        })[MyViewModel::class.java]
    }

    private fun initAdapter() {
        val adapter = PostsAdapter {
            model.retry()
        }
        list.adapter = adapter
        model.posts.observe(this, Observer<PagedList<ClientEvent>> {
            adapter.submitList(it)
        })
        model.networkState.observe(this, Observer {
            adapter.setNetworkState(it)
        })
    }

    private fun initSwipeToRefresh() {
        model.refreshState.observe(this, Observer {
            swipe_refresh.isRefreshing = it == NetworkState.LOADING
        })
        swipe_refresh.setOnRefreshListener {
            model.refresh()
        }
    }
}
