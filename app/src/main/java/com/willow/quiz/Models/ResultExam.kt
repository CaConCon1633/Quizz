package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class ResultExam(
    @SerializedName("count_correct")
    val countCorrect: Int?,
    @SerializedName("count_questions")
    val countQuestions: Int?,
    @SerializedName("result")
    val result: Double?
)