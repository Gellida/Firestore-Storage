package com.example.firestore.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.firestore.ui.task.TaskViewModel
import com.example.firestore.databinding.FragmentDashboardBinding
import com.example.firestore.ui.task.NewTaskFragment
import com.example.firestore.ui.task.TaskAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseFirestore : FirebaseFirestore
    private lateinit var taskViewModel: TaskViewModel
    private val taskList = mutableListOf<Task>()
    private lateinit var adapter: TaskAdapter



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        taskViewModel = ViewModelProvider(this)[TaskViewModel::class.java]
        initVars()


        dashboardViewModel.text.observe(viewLifecycleOwner) {
        }

        TaskRealtimeUpdates()

        binding.fabAdd.setOnClickListener {
            NewTaskFragment().show(parentFragmentManager,"newTaskTag")
        }


        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = TaskAdapter(taskList)
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // No se utiliza para este ejemplo
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val deletedTaskTitle = taskList[position].title
                adapter.deleteTask(position,deletedTaskTitle)

                /*
                Snackbar.make(viewHolder.itemView, "$deletedTaskTitle eliminada", Snackbar.LENGTH_LONG)
                    .setAction("Deshacer") {
                        taskList.add(position, Task(deletedTaskTitle, ""))
                        //notifyItemInserted(position)
                    }
                    .show()

                 */

            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.recyclerView)
        binding.recyclerView.adapter = adapter
    }
    private fun initVars(){
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val TodoTaskRef = firebaseFirestore.collection("Todo")

    }
    private fun TaskRealtimeUpdates(){
        val TodoTaskRef = firebaseFirestore.collection("Todo")
        TodoTaskRef.addSnapshotListener { querySnapshot, error ->
            error?.let {
                Toast.makeText(requireContext(),it.message,Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            querySnapshot?.let {
                taskList.clear()
                for (document in it){
                    val task = document.toObject(Task::class.java)
                    taskList.add(task)
                }
                adapter.updateData(taskList)
            }
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}