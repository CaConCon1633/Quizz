package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class Report(
    @SerializedName("exams")
    val exams: List<Exam?>?,
    @SerializedName("exams_count")
    val examsCount: Int?,
    @SerializedName("joined_exams_count")
    val joinedExamsCount: Int?,
    @SerializedName("results_count")
    val resultsCount: Int?
) {
    data class Exam(
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("description")
        val description: Any?,
        @SerializedName("done_count")
        val doneCount: Int?,
        @SerializedName("duration")
        val duration: Int?,
        @SerializedName("editable")
        val editable: Boolean?,
        @SerializedName("exam_id")
        val examId: String?,
        @SerializedName("greater_5")
        val greater5: Int?,
        @SerializedName("short_id")
        val shortId: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("updated_at")
        val updatedAt: String?,
        @SerializedName("user_id")
        val userId: String?
    )
}