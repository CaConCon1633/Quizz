package com.willow.quiz.Models

import com.google.gson.annotations.SerializedName

data class ShortId(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("description")
    val description: Any?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("exam_id")
    val examId: String?,
    @SerializedName("questions")
    val questions: List<Exam.Question?>?,
    @SerializedName("short_id")
    val shortId: String?,
    @SerializedName("status")
    val status: String?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("questions_count")
    val questionsCount: Int?,
    @SerializedName("count_correct")
    val countCorrect: Int?,
    @SerializedName("count_questions")
    val countQuestions: Int?,
    @SerializedName("result")
    val result: Int?
)
