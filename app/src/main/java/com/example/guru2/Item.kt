package com.example.guru2

data class Item(
    val id: Int = 0,
    var name: String,
    var category: String,
    var quantity: Int,
    var memo: String,
    var isCompleted: Boolean = false
)
