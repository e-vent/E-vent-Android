package io.github.e_vent.viewerui

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
    private val eventName: TextView = view.findViewById(R.id.event_name)
    private val eventDesc: TextView = view.findViewById(R.id.event_desc)
    private var post : ClientEvent? = null
    init {
//        view.setOnClickListener {
//        }
    }

    fun bind(post: ClientEvent?) {
        this.post = post
        eventName.text = post?.name ?: "loading"
        eventDesc.text = post?.desc ?: "..."
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