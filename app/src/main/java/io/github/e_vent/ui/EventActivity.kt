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
import io.github.e_vent.vo.Event
import kotlinx.android.synthetic.main.activity_event.*

/**
 * A list activity that shows reddit posts in the given sub-reddit.
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
        model.posts.observe(this, Observer<PagedList<Event>> {
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
