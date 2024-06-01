package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class UpdateExam(
    @SerializedName("description")
    val description: Any?,
    @SerializedName("duration")
    val duration: Int?,
    @SerializedName("questions")
    val questions: List<Exam.Question>,
    @SerializedName("status")
    val status: String?,
    @SerializedName("title")
    val title: String?
) {
    data class Question(
        @SerializedName("answers")
        val answers: List<Answer?>?,
        @SerializedName("correct")
        val correct: Int?,
        @SerializedName("question")
        val question: String?
    ) {
        data class Answer(
            @SerializedName("answer")
            val answer: String?
        )
    }
}