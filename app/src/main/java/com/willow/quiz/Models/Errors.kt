package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class Errors(
    @SerializedName("errors")
    val errors: Errors?,
    @SerializedName("message")
    val message: String?
) {
    data class Errors(
        @SerializedName("email")
        val email: List<String?>?,
        @SerializedName("password")
        val password: List<String?>?
    )
}