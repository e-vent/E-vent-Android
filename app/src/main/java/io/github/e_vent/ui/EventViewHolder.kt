package io.github.e_vent.ui

import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.github.e_vent.R
import io.github.e_vent.vo.Event

/**
 * A RecyclerView ViewHolder that displays a post.
 */
class EventViewHolder(view: View)
    : RecyclerView.ViewHolder(view) {
    private val title: TextView = view.findViewById(R.id.title)
    private val subtitle: TextView = view.findViewById(R.id.subtitle)
    private val score: TextView = view.findViewById(R.id.score)
    private var post : Event? = null
    init {
        view.setOnClickListener {
            post?.url?.let { url ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                view.context.startActivity(intent)
            }
        }
    }

    fun bind(post: Event?) {
        this.post = post
        title.text = post?.title ?: "loading"
        subtitle.text = itemView.context.resources.getString(R.string.post_subtitle,
                post?.author ?: "unknown")
        score.text = "${post?.score ?: 0}"
    }

    companion object {
        fun create(parent: ViewGroup): EventViewHolder {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.event_item, parent, false)
            return EventViewHolder(view)
        }
    }

    fun updateScore(item: Event?) {
        post = item
        score.text = "${item?.score ?: 0}"
    }
}