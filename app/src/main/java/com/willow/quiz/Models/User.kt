package com.willow.quiz.Models


import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("details")
    val details: Details?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: Any?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("updated_at")
    val updatedAt: String?,
    @SerializedName("user_id")
    val userId: Int?
) {
    data class Details(
        @SerializedName("dateOfBirth")
        val dateOfBirth: Any?,
        @SerializedName("gender")
        val gender: String?,
        @SerializedName("phone")
        val phone: Any?,
        @SerializedName("user_id")
        val userId: Int?
    )
}