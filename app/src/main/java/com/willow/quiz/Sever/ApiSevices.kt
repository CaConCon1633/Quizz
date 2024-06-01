package com.willow.quiz.Sever

import com.willow.quiz.Models.Answers
import com.willow.quiz.Models.Exam
import com.willow.quiz.Models.JoinExam
import com.willow.quiz.Models.LoginResponse
import com.willow.quiz.Models.Message
import com.willow.quiz.Models.ResultExam
import com.willow.quiz.Models.ShortId
import com.willow.quiz.Models.UpdateExam
import com.willow.quiz.Models.User
import com.willow.quiz.Models.UserInfor
import com.willow.quiz.Models.UserResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiSevices {

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @FormUrlEncoded
    @POST("register")
    fun create(
        @Field("email") email: String,
        @Field("name") name: String,
        @Field("password") password: String,
        @Field("password_confirmation") passwordConf: String
    ): Call<LoginResponse>

    @FormUrlEncoded
    @POST("login")
    fun login(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>


    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @GET("user")
    fun getUser(
        @Header("Authorization") token: String
    ): Call<User>

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @PUT("user")
    fun putUser(
        @Header("Authorization") token: String,
        @Body user: UserInfor
    ):Call<UserResponse>


    @GET("exam/{short_id}/short")
    fun getExamId(
        @Header("Authorization") token: String,
       @Path("short_id") shortId: String
    ): Call<ShortId>

    @GET("exam/{exam_id}/join")
    fun getJoinExam(
        @Header("Authorization") token: String,
        @Path("exam_id") examId: String
    ):Call<JoinExam>


    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @POST("exam/{exam_id}")
    fun postSubmit(
        @Header("Authorization") token: String,
        @Path("exam_id") examId: String,
        @Body answers: Answers
    ):Call<ResultExam>

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @POST("exams")
    fun postCreate(
        @Header("Authorization") token: String
    ):Call<Exam>

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @GET("exams")
    fun getAllExam(
        @Header("Authorization") token: String
    ):Call<List<Exam>>

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @GET("exams/{exam_id}")
    fun getExam(
        @Path ("exam_id") examId: String,
        @Header("Authorization") token: String
    ):Call<Exam>

    @Headers(
        "Accept: application/vnd.github.v3.full+json",
        "User-Agent: Retrofit-Sample-App"
    )
    @PUT("exams/{exam_id}")
    fun upadteExam(
        @Path ("exam_id") examId: String,
        @Header("Authorization") token: String,
        @Body exam: UpdateExam
    ):Call<Exam>

    @DELETE("exams/{exam_id}")
    fun deleteExam(
        @Path ("exam_id") examId: String,
        @Header("Authorization") token: String
    ):Call<Message>
}