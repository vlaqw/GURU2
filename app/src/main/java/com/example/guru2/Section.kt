package com.example.guru2

data class Section(
    val title: String,
    var isExpanded: Boolean,
    val items: MutableList<Item>
)

