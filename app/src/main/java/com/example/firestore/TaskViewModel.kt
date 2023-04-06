package com.example.firestore

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TaskViewModel: ViewModel() {
    var title = MutableLiveData<String>()
    var desc = MutableLiveData<String>()
}