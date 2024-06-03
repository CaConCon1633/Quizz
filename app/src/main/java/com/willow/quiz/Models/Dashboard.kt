package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class Dashboard(
    @SerializedName("exams_count")
    val examsCount: Int?,
    @SerializedName("joined_exams_count")
    val joinedExamsCount: Int?,
    @SerializedName("results_count")
    val resultsCount: Int?
)