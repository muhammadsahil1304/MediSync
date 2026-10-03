package com.example.newmedisync.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.newmedisync.R
import com.example.newmedisync.model.DaySchedule
import com.google.android.material.switchmaterial.SwitchMaterial

class WeeklyScheduleAdapter(
    private var scheduleList: MutableList<DaySchedule>,
    private val onTimePickRequested: (position: Int, isStartTime: Boolean, currentText: String) -> Unit
) : RecyclerView.Adapter<WeeklyScheduleAdapter.DayScheduleViewHolder>() {

    fun getSchedulesMap(): Map<String, DaySchedule> {
        return scheduleList.associateBy { it.dayOfWeek }
    }

    fun updateSchedules(newList: List<DaySchedule>) {
        scheduleList = newList.toMutableList()
        notifyDataSetChanged()
    }

    fun updateTime(position: Int, isStartTime: Boolean, newTime: String) {
        if (position in scheduleList.indices) {
            val item = scheduleList[position]
            scheduleList[position] = if (isStartTime) {
                item.copy(startTime = newTime)
            } else {
                item.copy(endTime = newTime)
            }
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayScheduleViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weekly_schedule_day, parent, false)
        return DayScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayScheduleViewHolder, position: Int) {
        holder.bind(scheduleList[position], position)
    }

    override fun getItemCount(): Int = scheduleList.size

    inner class DayScheduleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDayName: TextView = itemView.findViewById(R.id.tvDayName)
        private val switchEnabled: SwitchMaterial = itemView.findViewById(R.id.switchEnabled)
        private val layoutTimePicker: View = itemView.findViewById(R.id.layoutTimePicker)
        private val btnStartTime: Button = itemView.findViewById(R.id.btnStartTime)
        private val btnEndTime: Button = itemView.findViewById(R.id.btnEndTime)

        fun bind(schedule: DaySchedule, position: Int) {
            tvDayName.text = schedule.dayOfWeek
            switchEnabled.isChecked = schedule.enabled
            btnStartTime.text = schedule.startTime
            btnEndTime.text = schedule.endTime

            layoutTimePicker.visibility = if (schedule.enabled) View.VISIBLE else View.GONE

            switchEnabled.setOnCheckedChangeListener(null)
            switchEnabled.isChecked = schedule.enabled
            switchEnabled.setOnCheckedChangeListener { _, isChecked ->
                scheduleList[position] = schedule.copy(enabled = isChecked)
                layoutTimePicker.visibility = if (isChecked) View.VISIBLE else View.GONE
            }

            btnStartTime.setOnClickListener {
                onTimePickRequested(position, true, schedule.startTime)
            }

            btnEndTime.setOnClickListener {
                onTimePickRequested(position, false, schedule.endTime)
            }
        }
    }
}
