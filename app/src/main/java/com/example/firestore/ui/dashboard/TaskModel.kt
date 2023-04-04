package com.example.firestore.ui.dashboard

data class Task(
    var title: String = "",
    var description: String = "",
    var imageUrl: String = ""
) {
    constructor() : this("", "", "")
}