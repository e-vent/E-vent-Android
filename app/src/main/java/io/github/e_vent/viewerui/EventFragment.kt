package io.github.e_vent.viewerui

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import io.github.e_vent.R
import io.github.e_vent.ServiceLocator
import io.github.e_vent.repo.NetworkState
import io.github.e_vent.vo.ClientEvent
import kotlinx.android.synthetic.main.event_fragment.*

class EventFragment : Fragment() {
    private lateinit var model: EventViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.event_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        model = ViewModelProviders.of(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                val repo = ServiceLocator.instance(this@EventFragment.requireContext())
                        .getRepository()
                @Suppress("UNCHECKED_CAST")
                return EventViewModel(repo) as T
            }
        })[EventViewModel::class.java]
        initAdapter()
        initSwipeToRefresh()
        model.show()
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
