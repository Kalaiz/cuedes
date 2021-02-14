package com.kalai.cuedes.alarm

import androidx.recyclerview.widget.DiffUtil
import com.kalai.cuedes.data.Alarm

class AlarmListDiffCallback: DiffUtil.ItemCallback<Alarm>() {
    override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem.name == newItem.name
    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean = oldItem == newItem
}
