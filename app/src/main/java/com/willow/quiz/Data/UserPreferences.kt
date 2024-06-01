package com.willow.quiz.Data

import android.content.Context
import android.content.SharedPreferences

class UserPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

    fun saveUserInfo(name: String, email: String, dob: String, phone: String, gender: String) {
        with(sharedPreferences.edit()) {
            putString("name", name)
            putString("email", email)
            putString("dob", dob)
            putString("phone", phone)
            putString("gender", gender)
            apply()
        }
    }

    fun getUserInfo(): Map<String, String?> {
        val name = sharedPreferences.getString("name", "")
        val email = sharedPreferences.getString("email", "")
        val dob = sharedPreferences.getString("dob", "")
        val phone = sharedPreferences.getString("phone", "")
        val gender = sharedPreferences.getString("gender", "")
        return mapOf(
            "name" to name,
            "email" to email,
            "dob" to dob,
            "phone" to phone,
            "gender" to gender
        )
    }

    fun deleteUserInfo() {
        with(sharedPreferences.edit()) {
            remove("name")
            remove("email")
            remove("dob")
            remove("phone")
            remove("gender")
            apply()
        }
    }
    fun updateUserInfo(name: String?, email: String?, dob: String?, phone: String?, gender: String?) {
        with(sharedPreferences.edit()) {
            name?.let { putString("name", it) }
            email?.let { putString("email", it) }
            dob?.let { putString("dob", it) }
            phone?.let { putString("phone", it) }
            gender?.let { putString("gender", it) }
            apply()
        }
    }
}
