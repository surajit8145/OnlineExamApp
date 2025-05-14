package com.example.onlineexamapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.onlineexamapp.R
import com.example.onlineexamapp.models.Announcement

class AnnouncementAdapter(private val announcements: List<Announcement>) :
    RecyclerView.Adapter<AnnouncementAdapter.AnnouncementViewHolder>() {

    inner class AnnouncementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.tvAnnouncementTitle)
        val contentText: TextView = itemView.findViewById(R.id.tvAnnouncementContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_announcement, parent, false)
        return AnnouncementViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        val announcement = announcements[position]
        holder.titleText.text = announcement.title
        holder.contentText.text = announcement.content
    }

    override fun getItemCount(): Int = announcements.size
}
