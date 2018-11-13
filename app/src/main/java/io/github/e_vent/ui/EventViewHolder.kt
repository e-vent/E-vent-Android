package io.github.e_vent.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.e_vent.R
import io.github.e_vent.vo.ClientEvent

/**
 * A RecyclerView ViewHolder that displays a post.
 */
class EventViewHolder(view: View)
    : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private var post : ClientEvent? = null
    init {
//        view.setOnClickListener {
//        }
    }

    fun bind(post: ClientEvent?) {
        this.post = post
        title.text = post?.name ?: "loading"
    }

    companion object {
        fun create(parent: ViewGroup): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.event_item, parent, false)
            return EventViewHolder(view)
        }
    }

    fun update(item: ClientEvent?) {
        post = item
    }
}