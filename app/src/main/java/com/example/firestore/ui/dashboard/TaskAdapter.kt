package com.example.firestore.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestore.databinding.TaskItemBinding
import com.squareup.picasso.Picasso

class TaskAdapter(private var mList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = TaskItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        with(holder.binding) {
            val task = mList[position]
            //holder.binding.description.text = task.description
            //Picasso.get().load(task.imageUrl).into(imagePreview)
        }
    }

    fun updateData(newList: MutableList<Task>) {
        mList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}