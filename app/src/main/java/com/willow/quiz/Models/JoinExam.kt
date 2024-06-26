package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class JoinExam(
    @SerializedName("exam")
    val exam: Exam?,
    @SerializedName("questions")
    val questions: List<Question?>?,
    @SerializedName("time_end")
    val timeEnd: String?,
    @SerializedName("time_left")
    val timeLeft: Int?
) {
    data class Exam(
        @SerializedName("created_at")
        val createdAt: String?,
        @SerializedName("description")
        val description: Any?,
        @SerializedName("duration")
        val duration: Int?,
        @SerializedName("exam_id")
        val examId: String?,
        @SerializedName("short_id")
        val shortId: String?,
        @SerializedName("status")
        val status: String?,
        @SerializedName("title")
        val title: String?,
        @SerializedName("updated_at")
        val updatedAt: String?,
        @SerializedName("user_id")
        val userId: Int?
    )

    data class Question(
        @SerializedName("answers")
        val answers: List<Answer?>?,
        @SerializedName("question")
        val question: String?
    ) {
        data class Answer(
            @SerializedName("answer")
            val answer: String?
        )
    }
}