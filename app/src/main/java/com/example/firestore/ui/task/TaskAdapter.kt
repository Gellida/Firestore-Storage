package com.example.firestore.ui.task

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.firestore.databinding.TaskItemBinding
import com.example.firestore.ui.dashboard.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class TaskAdapter(private var taskList: MutableList<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private val bucketName = storageRef.bucket
    private lateinit var firebaseFirestore : FirebaseFirestore


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
        val gsReference = Firebase.storage.getReferenceFromUrl("gs://$bucketName/${task.photoURL}")



        val imageRef = FirebaseStorage.getInstance().reference.bucket

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

    fun deleteTask(position: Int,title:String) {
        taskList.removeAt(position)
        notifyItemRemoved(position)
        firebaseFirestore = FirebaseFirestore.getInstance()
        val db = firebaseFirestore.collection("Todo")
        val query = db.whereEqualTo("title", title)

        query.get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.size() > 0) {
                val taskToDelete = querySnapshot.documents[0]
                taskToDelete.reference.delete().addOnSuccessListener {
                }.addOnFailureListener { exception ->
                }
            } else {
                Log.i("error","No sé encontro el titulo que estabas buscando")
            }
        }.addOnFailureListener { exception ->
            Log.i("error","Ocurrió un error al hacer la consulta")
        }
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}