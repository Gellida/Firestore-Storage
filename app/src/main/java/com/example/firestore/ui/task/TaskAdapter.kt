package com.example.firestore.ui.task

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.firestore.databinding.TaskItemBinding
import com.example.firestore.ui.dashboard.Task
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class TaskAdapter(private var taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val bucketName = storageRef.bucket

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

        val task = taskList[position]
        holder.binding.title.text = task.title
        holder.binding.description.text = task.description

        val gs = holder.binding.imagePreview
        val gsReference = Firebase.storage.getReferenceFromUrl("gs://$bucketName"+task.photoURL)



        val imageRef = storageRef.bucket

        Log.i("imageref", imageRef.toString())
        Log.i("gs", gsReference.toString())

        gsReference.downloadUrl
            .addOnSuccessListener { uri ->
                Picasso.get().load(uri).into(gs)
            }
            .addOnFailureListener { exception ->

            }

}

    fun updateData(newList: MutableList<Task>) {
        taskList = newList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}