package com.example.firestore.ui.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firestore.databinding.FragmentDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore

class DashboardFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var firebaseFirestore : FirebaseFirestore
    private var mList = mutableListOf<String>()
    //private var mList = mutableListOf<Task>()
    private lateinit var adapter: ImagesAdapter
    //private lateinit var adapter: TaskAdapter
    private val TAG = "DashboardFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root
        initVars()
        getImages()


        dashboardViewModel.text.observe(viewLifecycleOwner) {
        }


        return root
    }
    private fun initVars(){
        firebaseFirestore = FirebaseFirestore.getInstance()
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ImagesAdapter(mList)
        //adapter = TaskAdapter(mList)
        binding.recyclerView.adapter = adapter

    }
    @SuppressLint("NotifyDataSetChanged")
    private fun getImages(){

        firebaseFirestore.collection("Images")
            .get().addOnSuccessListener {
                for (i in it){
                    mList.add(i.data["picture"].toString())
                }
                adapter.notifyDataSetChanged()
            }

         /*
        firebaseFirestore.collection("Tasks")
            .get().addOnSuccessListener {
                val taskList = mutableListOf<Task>()
                Log.i(TAG,it.toString())

                for (i in it){
                    val task = i.toObject(Task::class.java)
                    taskList.add(task)
                }
                adapter.updateData(taskList)
            }
        */

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}