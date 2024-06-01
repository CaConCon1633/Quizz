package com.willow.quiz.Models

data class Question(
    val id: Int,
    val question: String,
    val correct: Int,
    val answers: List<String>
)
