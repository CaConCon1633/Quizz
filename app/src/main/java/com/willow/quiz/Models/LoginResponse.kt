package com.willow.quiz.Models

data class LoginResponse(
    val message: String,
    val errors: Errors,
    val token: String,
    val user: User
)
