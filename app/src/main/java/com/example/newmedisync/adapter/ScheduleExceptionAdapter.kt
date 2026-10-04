package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.ScheduleException

class ScheduleExceptionAdapter(
    private var exceptionList: List<ScheduleException>,
    private val onDeleteRequested: (ScheduleException) -> Unit
) : RecyclerView.Adapter<ScheduleExceptionAdapter.ExceptionViewHolder>() {

    fun updateList(newList: List<ScheduleException>) {
        exceptionList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExceptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_schedule_exception, parent, false)
        return ExceptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExceptionViewHolder, position: Int) {
        holder.bind(exceptionList[position])
    }

    override fun getItemCount(): Int = exceptionList.size

    inner class ExceptionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvExceptionDate: TextView = itemView.findViewById(R.id.tvExceptionDate)
        private val tvExceptionType: TextView = itemView.findViewById(R.id.tvExceptionType)
        private val tvExceptionReason: TextView = itemView.findViewById(R.id.tvExceptionReason)
        private val btnDeleteException: Button = itemView.findViewById(R.id.btnDeleteException)

        fun bind(exception: ScheduleException) {
            tvExceptionDate.text = exception.date

            if (exception.type.equals("FULL_DAY", ignoreCase = true)) {
                tvExceptionType.text = "Full Day Unavailable"
            } else {
                tvExceptionType.text = "${exception.startTime} – ${exception.endTime} Blocked"
            }

            if (exception.reason.isNotBlank()) {
                tvExceptionReason.visibility = View.VISIBLE
                tvExceptionReason.text = "Reason: ${exception.reason}"
            } else {
                tvExceptionReason.visibility = View.GONE
            }

            btnDeleteException.setOnClickListener {
                onDeleteRequested(exception)
            }
        }
    }
}
