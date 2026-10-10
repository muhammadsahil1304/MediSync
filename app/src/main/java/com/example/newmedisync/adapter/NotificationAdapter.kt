package com.example.newmedisync.adapter

import android.graphics.Color
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.NotificationItem
import com.google.android.material.card.MaterialCardView

class NotificationAdapter(
    private var list: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    fun updateList(newList: List<NotificationItem>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardNotification: MaterialCardView = itemView.findViewById(R.id.cardNotification)
        private val imgTypeIcon: ImageView = itemView.findViewById(R.id.imgTypeIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        private val tvMessage: TextView = itemView.findViewById(R.id.tvMessage)
        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val viewUnreadDot: View = itemView.findViewById(R.id.viewUnreadDot)

        fun bind(item: NotificationItem) {
            tvTitle.text = item.title.ifBlank { "Notification" }
            tvMessage.text = item.message.ifBlank { "You have a new update." }

            val timeAgo = DateUtils.getRelativeTimeSpanString(
                item.timestamp,
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            tvTime.text = timeAgo

            // Read vs Unread styling
            if (item.isRead) {
                viewUnreadDot.visibility = View.GONE
                cardNotification.setCardBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                viewUnreadDot.visibility = View.VISIBLE
                cardNotification.setCardBackgroundColor(Color.parseColor("#F0F6FF"))
            }

            // Type icons & colors
            val context = itemView.context
            when (item.type) {
                "APPOINTMENT_BOOKED" -> {
                    imgTypeIcon.setImageResource(R.drawable.calender)
                    imgTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue))
                }
                "APPOINTMENT_COMPLETED" -> {
                    imgTypeIcon.setImageResource(R.drawable.undo)
                    imgTypeIcon.setColorFilter(Color.parseColor("#16A34A"))
                }
                "APPOINTMENT_CANCELLED" -> {
                    imgTypeIcon.setImageResource(R.drawable.bell)
                    imgTypeIcon.setColorFilter(Color.parseColor("#DC2626"))
                }
                "PRESCRIPTION_ISSUED" -> {
                    imgTypeIcon.setImageResource(R.drawable.pdf)
                    imgTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue))
                }
                else -> {
                    imgTypeIcon.setImageResource(R.drawable.bell)
                    imgTypeIcon.setColorFilter(ContextCompat.getColor(context, R.color.primaryBlue))
                }
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
